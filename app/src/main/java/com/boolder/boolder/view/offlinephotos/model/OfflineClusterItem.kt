package com.boolder.boolder.view.offlinephotos.model

data class OfflineClusterItem(
    val name: String,
    val status: OfflineClusterItemStatus
)

enum class OfflineClusterItemStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED
}
