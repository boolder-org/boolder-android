package com.boolder.boolder.utils.extension

import com.mapbox.maps.CameraOptions

fun CameraOptions.coerceZoomAtLeast(minZoom: Double): CameraOptions {
    val zoom = zoom ?: return this

    return CameraOptions.Builder()
        .center(center)
        .padding(padding)
        .bearing(bearing)
        .pitch(pitch)
        .zoom(zoom.coerceAtLeast(minZoom))
        .build()
}
