package com.boolder.boolder.offline

interface OfflineClusterDownloader {
    fun onDownloadCluster()
    fun onCancelClusterDownload()
    fun onDeleteClusterPhotos()
}

fun dummyOfflineClusterDownloader() = object : OfflineClusterDownloader {
    override fun onDownloadCluster() {}
    override fun onCancelClusterDownload() {}
    override fun onDeleteClusterPhotos() {}
}
