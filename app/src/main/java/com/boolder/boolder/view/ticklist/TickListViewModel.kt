package com.boolder.boolder.view.ticklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.userdatabase.repository.TickedProblemRepository
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.Problem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TickListViewModel(
    private val tickedProblemRepository: TickedProblemRepository,
    private val problemRepository: ProblemRepository,
    private val areaRepository: AreaRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

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

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Content(
            val problems: List<Problem>
        ) : ScreenState
    }
}
