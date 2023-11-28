package com.boolder.boolder.offline.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.BoolderMapConfig
import com.boolder.boolder.offline.WORK_DATA_PROGRESS
import com.boolder.boolder.offline.WORK_DATA_PROGRESS_DETAIL
import com.boolder.boolder.utils.extension.getAllTileRegionsAsync
import com.boolder.boolder.utils.extension.loadTileRegionAsync
import com.mapbox.bindgen.Value
import com.mapbox.common.NetworkRestriction
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.OfflineManager
import com.mapbox.maps.TilesetDescriptorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapTilesDownloadWorker(
    appContext: Context,
    params: WorkerParameters,
    private val areaRepository: AreaRepository,
    private val offlineManager: OfflineManager,
    private val tileStore: TileStore
) : CoroutineWorker(appContext, params) {

    private var retryCount = 0

    override suspend fun doWork(): Result {
        val areaId = inputData.getInt("areaId", -1)
            .takeIf { it >= 0 }
            ?: return Result.failure()

        val areaGeometry = areaRepository.getAreaById(areaId)
            .convert()
            .let {
                val northLat = it.northEastLat.toDouble()
                val southLat = it.southWestLat.toDouble()
                val westLng = it.southWestLon.toDouble()
                val eastLng = it.northEastLon.toDouble()

                Polygon.fromLngLats(
                    listOf(
                        listOf(
                            Point.fromLngLat(westLng, northLat),
                            Point.fromLngLat(eastLng, northLat),
                            Point.fromLngLat(eastLng, southLat),
                            Point.fromLngLat(westLng, southLat),
                        )
                    )
                )
            }

        return withContext(Dispatchers.Main) {
            downloadTileRegion(
                areaId = areaId,
                areaGeometry = areaGeometry
            )
        }
    }

    private suspend fun downloadTileRegion(
        areaId: Int,
        areaGeometry: Geometry
    ): Result {
        setProgress(
            workDataOf(
                WORK_DATA_PROGRESS to 0,
                WORK_DATA_PROGRESS_DETAIL to "0%"
            )
        )

        val id = "map-tiles-area-$areaId"

        val tilesetDescriptor = offlineManager.createTilesetDescriptor(
            TilesetDescriptorOptions.Builder()
                .styleURI(BoolderMapConfig.styleUri)
                .minZoom(0)
                .maxZoom(16)
                .build()
        )

        val tileRegionLoadOptions = TileRegionLoadOptions.Builder()
            .geometry(areaGeometry)
            .descriptors(listOf(tilesetDescriptor))
            .acceptExpired(false)
            .networkRestriction(NetworkRestriction.NONE)
            .metadata(Value(id))
            .build()

        val tileRegionLoadingResult = tileStore.loadTileRegionAsync(
            id = id,
            tileRegionLoadOptions = tileRegionLoadOptions,
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

                Log.d(TAG, "Tile pack download progress = $progressRatio")
            }
        )

        if (tileRegionLoadingResult.isValue) {
            Log.d(TAG, "Tile pack download completed (exp: ${tileRegionLoadingResult.value?.expires})")
        } else {
            Log.e(TAG, "Tile pack download failed (${tileRegionLoadingResult.error?.message})")

            if (retryCount < RETRY_LIMIT) return Result.retry()

            return Result.failure()
        }

        tileStore.getAllTileRegionsAsync().value?.let { tileRegionList ->
            Log.d(TAG, "Existing tile regions: $tileRegionList")
        }

        return Result.success()
    }

    companion object {
        private val TAG = MapTilesDownloadWorker::class.java.simpleName

        private const val RETRY_LIMIT = 3
    }
}
