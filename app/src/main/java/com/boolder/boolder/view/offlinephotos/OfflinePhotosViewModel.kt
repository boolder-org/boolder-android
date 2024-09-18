package com.boolder.boolder.view.offlinephotos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.offline.BoolderOfflineRepository
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class OfflinePhotosViewModel : ViewModel(), OfflineAreaDownloader {
    abstract val screenState: StateFlow<ScreenState>

    data class ScreenState(val items: List<OfflineAreaItem> = emptyList())
}

class OfflinePhotosViewModelImpl(
    private val areaRepository: AreaRepository,
    private val boolderOfflineRepository: BoolderOfflineRepository
) : OfflinePhotosViewModel() {

    override val screenState = MutableStateFlow(ScreenState())

    init {
        viewModelScope.launch {
            val allAreas = areaRepository.getAllAreas()

            combine(
                flows = allAreas.map { boolderOfflineRepository.getStatusForAreaIdFlow(it.id) },
                transform = { allStatuses ->
                    allStatuses.mapIndexed { index, offlineAreaItemStatus ->
                        OfflineAreaItem(
                            area = allAreas[index],
                            status = offlineAreaItemStatus
                        )
                    }
                }
            ).collect { items ->
                screenState.update { it.copy(items = items) }
            }
        }
    }

    // region OfflineAreaDownloader

    override fun onDownloadArea(areaId: Int) {
        boolderOfflineRepository.downloadArea(areaId)
    }

    override fun onCancelAreaDownload(areaId: Int) {
        boolderOfflineRepository.cancelAreaDownload(areaId)
        onDeleteAreaPhotos(areaId)
    }

    override fun onDeleteAreaPhotos(areaId: Int) {
        boolderOfflineRepository.deleteArea(areaId)
    }

    // endregion OfflineAreaDownloader
}
