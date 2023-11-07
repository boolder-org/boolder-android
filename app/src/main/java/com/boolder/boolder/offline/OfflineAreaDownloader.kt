package com.boolder.boolder.offline

interface OfflineAreaDownloader {
    fun onDownloadArea(areaId: Int)
    fun onCancelAreaDownload(areaId: Int)
    fun onAreaDownloadTerminated(areaId: Int)
}

fun dummyOfflineAreaDownloader() = object : OfflineAreaDownloader {
    override fun onDownloadArea(areaId: Int) {}
    override fun onCancelAreaDownload(areaId: Int) {}
    override fun onAreaDownloadTerminated(areaId: Int) {}
}
