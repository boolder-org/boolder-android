package com.boolder.boolder.offline.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.network.repository.TopoRepository
import com.boolder.boolder.offline.FileExplorer
import com.mapbox.common.TileStore
import com.mapbox.maps.OfflineManager

class BoolderWorkerFactory(
    private val areaRepository: AreaRepository,
    private val topoRepository: TopoRepository,
    private val fileExplorer: FileExplorer,
    private val offlineManager: OfflineManager,
    private val tileStore: TileStore
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? =
        when (workerClassName) {
            MapStylePackDownloadWorker::class.java.name -> MapStylePackDownloadWorker(
                appContext = appContext,
                params = workerParameters,
                offlineManager = offlineManager
            )

            MapTilesDownloadWorker::class.java.name -> MapTilesDownloadWorker(
                appContext = appContext,
                params = workerParameters,
                areaRepository = areaRepository,
                offlineManager = offlineManager,
                tileStore = tileStore
            )

            PhotosDownloadWorker::class.java.name -> PhotosDownloadWorker(
                appContext = appContext,
                params = workerParameters,
                topoRepository = topoRepository,
                fileExplorer = fileExplorer
            )

            else -> null
        }
}
