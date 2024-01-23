package com.boolder.boolder.view.discover.levels.beginner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.CircuitRepository
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Circuit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BeginnerLevelsViewModel(
    private val areaRepository: AreaRepository,
    private val circuitRepository: CircuitRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            val beginnerFriendlyAreas = areaRepository.getAllBeginnerFriendlyAreas()
            val areasWithCircuits = beginnerFriendlyAreas.map { area ->
                val beginnerFriendlyCircuits = circuitRepository.getBeginnerFriendlyCircuits(area.id)

                area to beginnerFriendlyCircuits
            }

            _screenState.update {
                ScreenState.Content(
                    areasWithCircuits = areasWithCircuits
                )
            }
        }
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Content(
            val areasWithCircuits: List<Pair<Area, List<Circuit>>>
        ) : ScreenState
    }
}
