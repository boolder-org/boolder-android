package com.boolder.boolder.view.discover.levels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.domain.model.Area
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LevelsViewModel(
    private val areaRepository: AreaRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            val allAreas = areaRepository.getAllAreasByProblemsCount()
            val beginnerAreas = areaRepository.getAllBeginnerFriendlyAreas()

            _screenState.update {
                ScreenState.Content(
                    beginnerAreas = beginnerAreas,
                    allAreas = allAreas
                )
            }
        }
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Content(
            val beginnerAreas: List<Area>,
            val allAreas: List<Area>
        ) : ScreenState
    }
}
