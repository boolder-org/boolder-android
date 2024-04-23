package com.boolder.boolder.offline.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.boolder.boolder.data.network.repository.TopoRepository
import com.boolder.boolder.offline.FileExplorer

class BoolderWorkerFactory(
    private val topoRepository: TopoRepository,
    private val fileExplorer: FileExplorer
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? =
        when (workerClassName) {
            PhotosDownloadWorker::class.java.name -> PhotosDownloadWorker(
                appContext = appContext,
                params = workerParameters,
                topoRepository = topoRepository,
                fileExplorer = fileExplorer
            )

            else -> null
        }
}
