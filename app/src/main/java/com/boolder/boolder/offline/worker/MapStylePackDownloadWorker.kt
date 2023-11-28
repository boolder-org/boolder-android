package com.boolder.boolder.offline.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.boolder.boolder.domain.model.BoolderMapConfig
import com.boolder.boolder.offline.WORK_DATA_PROGRESS
import com.boolder.boolder.offline.WORK_DATA_PROGRESS_DETAIL
import com.boolder.boolder.utils.extension.getAllStylePacksAsync
import com.boolder.boolder.utils.extension.loadStylePackAsync
import com.mapbox.bindgen.Value
import com.mapbox.maps.GlyphsRasterizationMode
import com.mapbox.maps.OfflineManager
import com.mapbox.maps.StylePackLoadOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapStylePackDownloadWorker(
    appContext: Context,
    params: WorkerParameters,
    private val offlineManager: OfflineManager
) : CoroutineWorker(appContext, params) {

    private var retryCount = 0

    override suspend fun doWork(): Result =
        withContext(Dispatchers.Main) { downloadStylePack() }

    private suspend fun downloadStylePack(): Result {
        offlineManager.getAllStylePacksAsync().value?.let { stylePackList ->
            Log.d(TAG, "Existing style packs: $stylePackList")

            if (stylePackList.any { it.styleURI == BoolderMapConfig.styleUri }) {
                return Result.success()
            }
        }

        setProgress(
            workDataOf(
                WORK_DATA_PROGRESS to 0,
                WORK_DATA_PROGRESS_DETAIL to "0%"
            )
        )

        val stylePackLoadOptions = StylePackLoadOptions.Builder()
            .glyphsRasterizationMode(GlyphsRasterizationMode.IDEOGRAPHS_RASTERIZED_LOCALLY)
            .acceptExpired(false)
            .metadata(Value("boolder-map-style-pack"))
            .build()

        val stylePackLoadingResult = offlineManager.loadStylePackAsync(
            styleUri = BoolderMapConfig.styleUri,
            stylePackLoadOptions = stylePackLoadOptions,
            onProgress = { progress ->
                val progressRatio = with(progress) {
                    completedResourceCount.toFloat() / requiredResourceCount.toFloat()
                }

                setProgressAsync(
                    workDataOf(
                        WORK_DATA_PROGRESS to progressRatio,
                        WORK_DATA_PROGRESS_DETAIL to String.format("%.2f%%", progressRatio * 100)
                    )
                )

                Log.d(TAG, "Style pack progress = $progressRatio")
            }
        )

        if (stylePackLoadingResult.isValue) {
            Log.d(TAG, "Style pack download completed (exp: ${stylePackLoadingResult.value?.expires})")
        } else {
            Log.e(TAG, "Style pack download failed (${stylePackLoadingResult.error?.message})")

            if (retryCount < RETRY_LIMIT) return Result.retry()

            return Result.failure()
        }

        offlineManager.getAllStylePacksAsync().value?.let { stylePackList ->
            Log.d(TAG, "Existing style packs: $stylePackList")
        }

        return Result.success()
    }

    companion object {
        private val TAG = MapStylePackDownloadWorker::class.java.simpleName

        private const val RETRY_LIMIT = 3
    }
}
