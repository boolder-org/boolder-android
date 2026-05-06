package com.boolder.boolder.view.map.filter.steepness

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.view.map.MapViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SteepnessFilterViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _screenStateFlow = MutableStateFlow(
        ScreenState(
            selectedSteepnesses = savedStateHandle.get<Array<String>>(ARG_SELECTED_STEEPNESSES)
                ?.toList()
                ?: MapViewModel.ALL_STEEPNESSES
        )
    )
    val screenStateFlow = _screenStateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onSteepnessToggled(steepness: String) {
        _screenStateFlow.update { state ->
            val selectedSteepnesses = if (steepness in state.selectedSteepnesses) {
                state.selectedSteepnesses - steepness
            } else {
                state.selectedSteepnesses + steepness
            }.sortedBySteepnessOrder()

            state.copy(selectedSteepnesses = selectedSteepnesses)
        }
    }

    fun onSteepnessesReset() {
        viewModelScope.launch {
            _eventFlow.emit(Event.SteepnessesValidated(MapViewModel.ALL_STEEPNESSES))
        }
    }

    fun onSteepnessesValidated() {
        viewModelScope.launch {
            _eventFlow.emit(Event.SteepnessesValidated(_screenStateFlow.value.selectedSteepnesses))
        }
    }

    data class ScreenState(
        val selectedSteepnesses: List<String>
    )

    sealed interface Event {
        data class SteepnessesValidated(val steepnesses: List<String>) : Event
    }

    companion object {
        private const val ARG_SELECTED_STEEPNESSES = "selected_steepnesses"
    }
}

private fun List<String>.sortedBySteepnessOrder(): List<String> =
    MapViewModel.ALL_STEEPNESSES.filter { it in this }
