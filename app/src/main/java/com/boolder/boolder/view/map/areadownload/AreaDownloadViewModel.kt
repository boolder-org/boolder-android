package com.boolder.boolder.view.map.areadownload

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.ClusterRepository
import com.boolder.boolder.domain.model.center
import com.boolder.boolder.domain.model.centerDistanceFromLatLon
import com.boolder.boolder.offline.BoolderOfflineRepository
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.offline.OfflineClusterDownloader
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import com.boolder.boolder.view.offlinephotos.model.OfflineClusterItem
import com.boolder.boolder.view.offlinephotos.model.OfflineClusterItemStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AreaDownloadViewModel(
    savedStateHandle: SavedStateHandle,
    private val clusterRepository: ClusterRepository,
    private val areaRepository: AreaRepository,
    private val boolderOfflineRepository: BoolderOfflineRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel(), OfflineClusterDownloader, OfflineAreaDownloader {

    private val clusterId = requireNotNull(savedStateHandle.get<Int>("cluster_id"))
    private val closestAreaId = requireNotNull(savedStateHandle.get<Int>("closest_area_id"))
    private val areaIds = requireNotNull(savedStateHandle.get<IntArray>("area_ids"))

    private val _screenState = MutableStateFlow(
        ScreenState(areasCount = areaIds.size)
    )
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            val clusterName = clusterRepository.getClusterById(clusterId)?.name ?: return@launch

            val closestArea = areaRepository.getAreaById(closestAreaId) ?: return@launch

            val closestAreaCenter = closestArea.center()
            val closestAreaCenterLat = closestAreaCenter.first.toDouble()
            val closestAreaCenterLon = closestAreaCenter.second.toDouble()

            val otherAreas = areaIds.filter { it != closestAreaId }
                .mapNotNull { areaRepository.getAreaById(it) ?: return@mapNotNull null }
                .sortedBy { it.centerDistanceFromLatLon(closestAreaCenterLat, closestAreaCenterLon) }

            val sortedAreas = listOf(closestArea) + otherAreas

            val areaItemsFlow = if (sortedAreas.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    flows = sortedAreas.map { nearbyArea ->
                        boolderOfflineRepository.getStatusForAreaIdFlow(nearbyArea.id)
                    },
                    transform = { nearbyAreaStatuses ->
                        nearbyAreaStatuses.mapIndexed { index, status ->
                            OfflineAreaItem(
                                area = sortedAreas[index],
                                status = status
                            )
                        }
                    }
                )
            }

            combine(
                flow = areaItemsFlow,
                flow2 = dataStore.data.map { it[SHOW_DOWNLOAD_HINT_KEY] ?: true },
                transform = { areaItems, showDownloadHint -> areaItems to showDownloadHint }
            ).collect { (areaItems, showDownloadHint) ->
                _screenState.update { state ->
                    state.copy(
                        content = Content(
                            clusterItem = OfflineClusterItem(
                                name = clusterName,
                                status = when {
                                    areaItems.any { it.status is OfflineAreaItemStatus.Downloading } -> OfflineClusterItemStatus.DOWNLOADING
                                    areaItems.all { it.status is OfflineAreaItemStatus.Downloaded } -> OfflineClusterItemStatus.DOWNLOADED
                                    else -> OfflineClusterItemStatus.NOT_DOWNLOADED
                                }
                            ),
                            areaItems = areaItems,
                            showDownloadHint = showDownloadHint
                        )
                    )
                }
            }
        }
    }

    // region OfflineClusterDownloader

    override fun onDownloadCluster() {
        areaIds.forEach(::onDownloadArea)
    }

    override fun onCancelClusterDownload() {
        val areaItems = screenState.value.content?.areaItems ?: return

        areaItems.filter { it.status is OfflineAreaItemStatus.Downloading }
            .forEach { onCancelAreaDownload(it.area.id) }
    }

    override fun onDeleteClusterPhotos() {
        areaIds.forEach(::onDeleteAreaPhotos)
    }

    // endregion OfflineClusterDownloader

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

        return currentScreenState.areaItems.find { it.area.id == areaId }
    }

    // endregion OfflineAreaDownloader

    fun onDismissDownloadHint() {
        viewModelScope.launch {
            dataStore.edit { it[SHOW_DOWNLOAD_HINT_KEY] = false }
        }
    }

    data class ScreenState(
        val areasCount: Int,
        val content: Content? = null
    )

    data class Content(
        val clusterItem: OfflineClusterItem,
        val areaItems: List<OfflineAreaItem> = emptyList(),
        val showDownloadHint: Boolean = false
    )

    companion object {
        private val SHOW_DOWNLOAD_HINT_KEY = booleanPreferencesKey("show_download_hint")
    }
}
