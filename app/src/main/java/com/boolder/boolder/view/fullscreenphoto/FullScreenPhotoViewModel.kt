package com.boolder.boolder.view.fullscreenphoto

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.ProblemWithLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FullScreenPhotoViewModel(
    savedStateHandle: SavedStateHandle,
    private val lineRepository: LineRepository,
    private val problemRepository: ProblemRepository
) : ViewModel() {

    private val problemId = requireNotNull(savedStateHandle.get<Int>("problem_id"))
    private val photoUri = requireNotNull(savedStateHandle.get<String>("photo_uri"))

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            val problem = problemRepository.problemById(problemId)
                ?.convert()
                ?: run {
                    _screenState.update { ScreenState.Error }
                    return@launch
                }

            val line = lineRepository.loadByProblemId(problemId)
                ?.convert()

            val completeProblem = CompleteProblem(
                problemWithLine = ProblemWithLine(
                    problem = problem,
                    line = line
                ),
                variants = emptyList()
            )

            _screenState.update {
                ScreenState.Content(
                    photoUri = photoUri,
                    completeProblem = completeProblem
                )
            }
        }
    }

    sealed interface ScreenState {
        data object Loading : ScreenState

        data class Content(
            val photoUri: String,
            val completeProblem: CompleteProblem
        ) : ScreenState

        data object Error : ScreenState
    }
}
