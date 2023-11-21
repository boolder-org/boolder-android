package com.boolder.boolder.view.discover.driesfast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.domain.model.Area
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DriesFastViewModel(
    private val areaRepository: AreaRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            val areas = areaRepository.getTaggedAreas("dry_fast")

            _screenState.update { ScreenState.Content(areas = areas) }
        }
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Content(
            val areas: List<Area>
        ) : ScreenState
    }
}
