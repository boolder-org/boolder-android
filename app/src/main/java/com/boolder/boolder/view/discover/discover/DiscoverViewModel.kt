package com.boolder.boolder.view.discover.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.domain.model.Area
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val areaRepository: AreaRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            val allAreas = areaRepository.getAllAreas()
            val popularAreas = areaRepository.getTaggedAreas("popular")

            _screenState.update {
                ScreenState.Content(
                    allAreas = allAreas,
                    popularAreas = popularAreas
                )
            }
        }
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Content(
            val allAreas: List<Area>,
            val popularAreas: List<Area>
        ) : ScreenState
    }
}
