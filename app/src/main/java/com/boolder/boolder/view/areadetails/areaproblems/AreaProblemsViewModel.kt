package com.boolder.boolder.view.areadetails.areaproblems

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.domain.model.Problem
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class AreaProblemsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val areaRepository: AreaRepository,
    private val problemRepository: ProblemRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    val screenState = _query
        .debounce(500L)
        .map { query ->
            val areaId = savedStateHandle.get<Int>("area_id") ?: -1
            val area = areaRepository.getAreaById(areaId) ?: return@map ScreenState.UnknownArea

            val allProblems = problemRepository.problemsForArea(areaId, query)
            val allPopularProblems = allProblems.filter { it.featured }

            ScreenState.Content(
                areaName = area.name,
                problems = allProblems,
                popularProblems = allPopularProblems
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ScreenState.Loading)

    fun onSearchQueryChanged(query: String) {
        _query.value = query
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Content(
            val areaName: String,
            val problems: List<Problem>,
            val popularProblems: List<Problem>
        ) : ScreenState

        data object UnknownArea : ScreenState
    }
}
