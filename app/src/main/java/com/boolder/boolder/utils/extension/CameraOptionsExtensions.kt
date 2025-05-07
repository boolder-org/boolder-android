package com.boolder.boolder.utils.extension

import com.mapbox.geojson.Point
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

fun fontainebleauCameraOptions(): CameraOptions =
    CameraOptions.Builder()
        .center(Point.fromLngLat(2.570619713818104, 48.40056240478899))
        .zoom(9.4)
        .build()
