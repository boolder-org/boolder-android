package com.boolder.boolder.offline.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.boolder.boolder.data.network.model.TopoUrl
import com.boolder.boolder.data.network.repository.TopoRepository
import com.boolder.boolder.offline.WORK_DATA_PROGRESS
import com.boolder.boolder.offline.WORK_DATA_PROGRESS_DETAIL
import com.boolder.boolder.offline.FileExplorer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File

class PhotosDownloadWorker(
    appContext: Context,
    params: WorkerParameters,
    private val topoRepository: TopoRepository,
    private val fileExplorer: FileExplorer
) : CoroutineWorker(appContext, params) {

    private val client = OkHttpClient()

    override suspend fun doWork(): Result {
        val areaId = inputData.getInt("areaId", -1)
            .takeIf { it >= 0 }
            ?: return Result.failure()

        val topoPhotoUrls = topoRepository.getTopoPicturesForArea(areaId)

        downloadPhotos(
            areaId = areaId,
            topoPhotoUrls = topoPhotoUrls
        )

        return Result.success()
    }

    private suspend fun downloadPhotos(
        areaId: Int,
        topoPhotoUrls: List<TopoUrl>
    ) {
        if (fileExplorer.areaFolderSize(areaId) > 0) return

        val assetsFolder = applicationContext.getDir(areaId.toString(), Context.MODE_PRIVATE)

        setProgress(
            workDataOf(
                WORK_DATA_PROGRESS to 0,
                WORK_DATA_PROGRESS_DETAIL to "0/${topoPhotoUrls.size}"
            )
        )

        withContext(Dispatchers.IO) {
            topoPhotoUrls.forEachIndexed { index, (topoId, imageUrl) ->
                downloadPhoto(assetsFolder, topoId, imageUrl)

                val progress = (index + 1).toFloat() / topoPhotoUrls.size.toFloat()

                Log.d(TAG, "Progress = $progress %")
                setProgress(
                    workDataOf(
                        WORK_DATA_PROGRESS to progress,
                        WORK_DATA_PROGRESS_DETAIL to "${index + 1}/${topoPhotoUrls.size}"
                    )
                )
            }
        }
    }

    private fun downloadPhoto(folder: File, topoId: Int, imageUrl: String) {
        val request = Request.Builder()
            .url(imageUrl)
            .build()

        client.newCall(request)
            .execute()
            .use { response ->
                if (!response.isSuccessful) {
                    Log.d(TAG, "Error downloading topo image $topoId, $imageUrl: $response")
                    return@use
                }

                val bufferedSource = response.body?.source() ?: return@use

                val imageFile = File(folder, topoId.toString())

                if (imageFile.exists()) return@use

                Log.d(TAG, "Writing $topoId - $imageUrl...")

                val bufferedSink = imageFile.sink().buffer()

                bufferedSink.writeAll(bufferedSource)
                bufferedSink.close()

                Log.d(TAG, "Successfully wrote $topoId - $imageUrl")
            }

        Log.d(TAG, "Downloads finished")
    }

    companion object {
        private const val TAG = "ContentDownloader"
    }
}
