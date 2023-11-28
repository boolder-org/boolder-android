package com.boolder.boolder.view.offlinephotos.model

import com.boolder.boolder.domain.model.Area

data class OfflineAreaItem(
    val area: Area,
    val status: OfflineAreaItemStatus
)

sealed interface OfflineAreaItemStatus {
    data object NotDownloaded : OfflineAreaItemStatus
    data class Downloaded(val folderSize: String) : OfflineAreaItemStatus
    data class Downloading(val areaId: Int) : OfflineAreaItemStatus
}
