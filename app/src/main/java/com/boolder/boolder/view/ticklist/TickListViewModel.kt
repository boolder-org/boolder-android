package com.boolder.boolder.view.ticklist

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.R
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.userdatabase.entity.TickStatus
import com.boolder.boolder.data.userdatabase.repository.TickedProblemRepository
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.TickedProblem
import com.boolder.boolder.view.ticklist.model.ExportableTick
import com.boolder.boolder.view.ticklist.model.ExportableTickList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class TickListViewModel(
    private val tickedProblemRepository: TickedProblemRepository,
    private val problemRepository: ProblemRepository,
    private val areaRepository: AreaRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    fun refreshState() {
        viewModelScope.launch {
            val allTickedProblems = tickedProblemRepository.getAllTickedProblems()

            val allProblems = allTickedProblems.mapNotNull {
                val problemEntity = problemRepository.problemById(it.problemId)
                    ?: return@mapNotNull null

                val areaName = areaRepository.getAreaById(problemEntity.areaId).name

                problemEntity.convert(
                    areaName = areaName,
                    tickStatus = it.tickStatus
                )
            }

            val sortedProblems = allProblems.sortedWith(
                compareBy(Problem::areaName)
                    .thenByDescending(Problem::grade)
                    .thenBy(Problem::name)
            )

            _screenState.update {
                ScreenState.Content(problems = sortedProblems)
            }
        }
    }

    fun onExportTickList() {
        viewModelScope.launch {
            val exportableTickedProblems = tickedProblemRepository.getAllTickedProblemIds()
                .map { ExportableTick(id = it) }

            val exportableProjectIds = tickedProblemRepository.getAllProjectIds()
                .map { ExportableTick(id = it) }

            val exportableTickList = ExportableTickList(
                tickedProblemIds = exportableTickedProblems,
                projectIds = exportableProjectIds
            )

            val json = Json.encodeToString(exportableTickList)
            val outputFile = File.createTempFile("Boolder_export", null)

            outputFile.writeText(json, charset = Charsets.UTF_8)

            outputFile.parent?.let { parentName ->
                val dateString = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
                val outputFileName = "Boolder_export_$dateString.json"
                val renamedFile = File(parentName, "Boolder_export_$dateString.json")

                outputFile.renameTo(renamedFile)

                outputFile.parentFile
                    ?.listFiles { _, name -> name != outputFileName && name.startsWith("Boolder_export_") }
                    ?.forEach { it.delete() }

                _events.emit(Event.TickListExportGenerated(file = renamedFile))
            }
        }
    }

    fun onChooseTickListToImport() {
        viewModelScope.launch { _events.emit(Event.ChooseTickListToImport) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun onImportTickList(inputStream: InputStream?) {
        inputStream ?: return

        val exportableTickList = try {
            Json.decodeFromStream<ExportableTickList>(inputStream)
        } catch (e: SerializationException) {
            emitErrorEvent(R.string.tick_list_error_import_malformed_input)
            null
        } catch (e: IllegalArgumentException) {
            emitErrorEvent(R.string.tick_list_error_import_malformed_input)
            null
        } catch (e: IOException) {
            emitErrorEvent(R.string.tick_list_error_import_cant_be_read)
            null
        } ?: return

        viewModelScope.launch {
            delay(500L)
            _events.emit(Event.AskForReplacementWhenImporting(exportableTickList))
        }
    }

    fun proceedToTickListImportation(
        exportableTickList: ExportableTickList,
        shouldReplaceAll: Boolean
    ) {
        viewModelScope.launch {
            if (shouldReplaceAll) tickedProblemRepository.deleteAll()

            exportableTickList.tickedProblemIds
                .forEach {
                    tickedProblemRepository.deleteTickedProblemByProblemId(it.id)
                    tickedProblemRepository.insertTickedProblem(
                        TickedProblem(
                            problemId = it.id,
                            tickStatus = TickStatus.SUCCEEDED
                        )
                    )
                }

            exportableTickList.projectIds
                .forEach {
                    tickedProblemRepository.deleteTickedProblemByProblemId(it.id)
                    tickedProblemRepository.insertTickedProblem(
                        TickedProblem(
                            problemId = it.id,
                            tickStatus = TickStatus.PROJECT
                        )
                    )
                }

            refreshState()
        }
    }

    private fun emitErrorEvent(@StringRes errorMessageId: Int) {
        viewModelScope.launch {
            delay(500L)
            _events.emit(Event.ShowError(errorMessageId))
        }
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Content(
            val problems: List<Problem>
        ) : ScreenState
    }

    sealed interface Event {
        data class TickListExportGenerated(val file: File) : Event
        data object ChooseTickListToImport : Event
        data class ShowError(@StringRes val errorMessageId: Int) : Event
        data class AskForReplacementWhenImporting(val exportableTickList: ExportableTickList) : Event
    }
}
