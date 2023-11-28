package com.boolder.boolder.view.offlinephotos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import com.boolder.boolder.offline.BoolderOfflineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class OfflinePhotosViewModel : ViewModel() {
    abstract val screenState: StateFlow<ScreenState>

    abstract fun onDownloadAreaClicked(areaId: Int)
    abstract fun onDownloadTerminated(areaId: Int)
    abstract fun onCancelDownload(areaId: Int)
    abstract fun onDeleteAreaClicked(areaId: Int)

    data class ScreenState(val items: List<OfflineAreaItem> = emptyList())
}

class OfflinePhotosViewModelImpl(
    private val areaRepository: AreaRepository,
    private val boolderOfflineRepository: BoolderOfflineRepository
) : OfflinePhotosViewModel() {

    override val screenState = MutableStateFlow(ScreenState())

    init {
        viewModelScope.launch {
            val items = areaRepository.getAllAreas()
                .map { area ->
                    OfflineAreaItem(
                        area = area,
                        status = boolderOfflineRepository.getStatusForAreaId(area.id)
                    )
                }

            val content = ScreenState(items = items)

            screenState.emit(content)
        }
    }

    override fun onDownloadAreaClicked(areaId: Int) {
        updateItemToDownloadingState(areaId)
        boolderOfflineRepository.downloadArea(areaId)
    }

    override fun onDownloadTerminated(areaId: Int) {
        updateItems(areaId)
    }

    override fun onCancelDownload(areaId: Int) {
        boolderOfflineRepository.cancelAreaDownload(areaId)
        onDeleteAreaClicked(areaId)
    }

    override fun onDeleteAreaClicked(areaId: Int) {
        boolderOfflineRepository.deleteArea(areaId)
        updateItems(areaId)
    }

    private fun updateItemToDownloadingState(areaId: Int) {
        val newItems = screenState.value.items.map { item ->
            if (item.area.id == areaId) {
                item.copy(
                    status = OfflineAreaItemStatus.Downloading(areaId = areaId)
                )
            } else {
                item
            }
        }

        screenState.update { it.copy(items = newItems) }
    }

    private fun updateItems(areaId: Int) {
        val newItems = screenState.value.items.map { item ->
            if (item.area.id == areaId) {
                item.copy(
                    area = item.area,
                    status = boolderOfflineRepository.getStatusForAreaId(item.area.id)
                )
            } else {
                item
            }
        }

        screenState.update { it.copy(items = newItems) }
    }
}
