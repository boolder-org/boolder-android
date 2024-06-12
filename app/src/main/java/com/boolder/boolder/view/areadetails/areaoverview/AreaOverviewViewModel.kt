package com.boolder.boolder.view.areadetails.areaoverview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.CircuitRepository
import com.boolder.boolder.domain.model.AccessFromPoi
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.offline.BoolderOfflineRepository
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AreaOverviewViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val areaRepository: AreaRepository,
    private val circuitRepository: CircuitRepository,
    private val boolderOfflineRepository: BoolderOfflineRepository
) : ViewModel(), OfflineAreaDownloader {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            val areaId = savedStateHandle.get<Int>("area_id") ?: return@launch
            val area = areaRepository.getAreaById(areaId) ?: run {
                _screenState.value = ScreenState.UnknownArea
                return@launch
            }

            val circuits = circuitRepository.getAvailableCircuits(areaId)
            val accessesFromPoi = areaRepository.getAccessesFromPoiByAreaId(areaId)

            boolderOfflineRepository.getStatusForAreaIdFlow(areaId)
                .collect { offlineAreaItemStatus ->
                    _screenState.value = ScreenState.Content(
                        area = area,
                        circuits = circuits,
                        accessesFromPoi = accessesFromPoi,
                        offlineAreaItemStatus = offlineAreaItemStatus
                    )
                }
        }
    }

    override fun onDownloadArea(areaId: Int) {
        boolderOfflineRepository.downloadArea(areaId)
    }

    override fun onCancelAreaDownload(areaId: Int) {
        boolderOfflineRepository.deleteArea(areaId)
        boolderOfflineRepository.cancelAreaDownload(areaId)
    }

    override fun onDeleteAreaPhotos(areaId: Int) {
        val currentState = _screenState.value as? ScreenState.Content ?: return

        boolderOfflineRepository.deleteArea(areaId)

        _screenState.update {
            currentState.copy(offlineAreaItemStatus = OfflineAreaItemStatus.NotDownloaded)
        }
    }

    sealed interface ScreenState {
        data object Loading : ScreenState

        data class Content(
            val area: Area,
            val circuits: List<Circuit>,
            val accessesFromPoi: List<AccessFromPoi>,
            val offlineAreaItemStatus: OfflineAreaItemStatus
        ) : ScreenState

        data object UnknownArea : ScreenState
    }
}
