package com.boolder.boolder.view.map.areadownload

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.offline.BoolderOfflineRepository
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AreaDownloadViewModel(
    savedStateHandle: SavedStateHandle,
    private val areaRepository: AreaRepository,
    private val boolderOfflineRepository: BoolderOfflineRepository
) : ViewModel(), OfflineAreaDownloader {

    private val areaId = requireNotNull(savedStateHandle.get<Int>("area_id"))
    private val nearbyAreaIds = requireNotNull(savedStateHandle.get<IntArray>("nearby_area_ids"))

    private val _screenState = MutableStateFlow(
        ScreenState(loadingNearbyItemsCount = nearbyAreaIds.size)
    )
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            val area = areaRepository.getAreaById(areaId) ?: return@launch

            val nearbyAreas = nearbyAreaIds
                .toList()
                .mapNotNull { areaRepository.getAreaById(it) ?: return@mapNotNull null }

            val nearbyAreaItemsFlow = if (nearbyAreas.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    flows = nearbyAreas.map { nearbyArea ->
                        boolderOfflineRepository.getStatusForAreaIdFlow(nearbyArea.id)
                    },
                    transform = { nearbyAreaStatuses ->
                        nearbyAreaStatuses.mapIndexed { index, status ->
                            OfflineAreaItem(
                                area = nearbyAreas[index],
                                status = status
                            )
                        }
                    }
                )
            }

            combine(
                flow = boolderOfflineRepository.getStatusForAreaIdFlow(areaId),
                flow2 = nearbyAreaItemsFlow,
                transform = { areaStatus, nearbyAreaItems -> areaStatus to nearbyAreaItems }
            ).collect { (offlineAreaItemStatus, nearbyAreaItems) ->
                _screenState.update {
                    ScreenState(
                        loadingNearbyItemsCount = null,
                        content = Content(
                            offlineAreaItem = OfflineAreaItem(
                                area = area,
                                status = offlineAreaItemStatus
                            ),
                            nearbyAreaItems = nearbyAreaItems
                        )
                    )
                }
            }
        }
    }

    // region OfflineAreaDownloader

    override fun onDownloadArea(areaId: Int) {
        val areaItem = checkArea(areaId)
            ?.takeIf { it.status is OfflineAreaItemStatus.NotDownloaded }
            ?: return

        boolderOfflineRepository.downloadArea(areaItem.area.id)
    }

    override fun onCancelAreaDownload(areaId: Int) {
        val areaItem = checkArea(areaId)
            ?.takeIf { it.status is OfflineAreaItemStatus.Downloading }
            ?: return

        boolderOfflineRepository.cancelAreaDownload(areaItem.area.id)
        boolderOfflineRepository.deleteArea(areaItem.area.id)
    }

    override fun onDeleteAreaPhotos(areaId: Int) {
        val areaItem = checkArea(areaId)
            ?.takeIf { it.status is OfflineAreaItemStatus.Downloaded }
            ?: return

        boolderOfflineRepository.deleteArea(areaItem.area.id)
    }

    private fun checkArea(areaId: Int): OfflineAreaItem? {
        val currentScreenState = screenState.value.content ?: return null

        return (currentScreenState.nearbyAreaItems + currentScreenState.offlineAreaItem)
            .find { it.area.id == areaId }
    }

    // endregion OfflineAreaDownloader

    data class ScreenState(
        val loadingNearbyItemsCount: Int?,
        val content: Content? = null
    )

    data class Content(
        val offlineAreaItem: OfflineAreaItem,
        val nearbyAreaItems: List<OfflineAreaItem> = emptyList()
    )
}
