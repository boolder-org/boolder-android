package com.boolder.boolder.view.discover.trainandbike

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.domain.model.AreaBikeRoute
import com.boolder.boolder.domain.model.TrainStationPoi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrainAndBikeViewModel(
    private val areaRepository: AreaRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            val poisMap = areaRepository.getTrainStationPoiWithBikeRoutes()

            _screenState.update {
                ScreenState.Content(poisMap = poisMap)
            }
        }
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Content(
            val poisMap: Map<TrainStationPoi, List<AreaBikeRoute>>
        ) : ScreenState
    }
}
