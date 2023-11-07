package com.boolder.boolder.utils.extension

import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileRegionLoadProgress
import com.mapbox.common.TileStore
import com.mapbox.maps.OfflineManager
import com.mapbox.maps.StylePackLoadOptions
import com.mapbox.maps.StylePackLoadProgress
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun OfflineManager.getAllStylePacksAsync() =
    suspendCoroutine { continuation ->
        getAllStylePacks(continuation::resume)
    }

suspend fun OfflineManager.loadStylePackAsync(
    styleUri: String,
    stylePackLoadOptions: StylePackLoadOptions,
    onProgress: (StylePackLoadProgress) -> Unit
) =
    suspendCancellableCoroutine { continuation ->
        val cancellableTask = loadStylePack(
            styleUri,
            stylePackLoadOptions,
            { onProgress(it) },
            { result -> continuation.resume(result) }
        )

        continuation.invokeOnCancellation { cancellableTask.cancel() }
    }

suspend fun TileStore.getAllTileRegionsAsync() =
    suspendCoroutine { continuation ->
        getAllTileRegions(continuation::resume)
    }

suspend fun TileStore.loadTileRegionAsync(
    id: String,
    tileRegionLoadOptions: TileRegionLoadOptions,
    onProgress: (TileRegionLoadProgress) -> Unit
) =
    suspendCancellableCoroutine { continuation ->
        val cancelableTask = loadTileRegion(
            id,
            tileRegionLoadOptions,
            { onProgress(it) },
            { result -> continuation.resume(result) }
        )

        continuation.invokeOnCancellation { cancelableTask.cancel() }
    }
