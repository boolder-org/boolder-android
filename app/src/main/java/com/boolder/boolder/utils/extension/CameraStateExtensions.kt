package com.boolder.boolder.utils.extension

import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState

fun CameraState.toCameraOptions(): CameraOptions =
    CameraOptions.Builder()
        .center(center)
        .padding(padding)
        .zoom(zoom)
        .build()
