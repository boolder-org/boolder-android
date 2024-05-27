package com.boolder.boolder.utils.extension

import android.os.Bundle
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets

private const val CENTER_LONGITUDE = "CAMERA_CENTER_LONGITUDE"
private const val CENTER_LATITUDE = "CAMERA_CENTER_LATITUDE"
private const val PADDING_TOP = "CAMERA_PADDING_TOP"
private const val PADDING_BOTTOM = "CAMERA_PADDING_BOTTOM"
private const val PADDING_LEFT = "CAMERA_PADDING_LEFT"
private const val PADDING_RIGHT = "CAMERA_PADDING_RIGHT"
private const val ZOOM = "CAMERA_ZOOM"
private const val BEARING = "CAMERA_BEARING"
private const val PITCH = "CAMERA_PITCH"

fun Bundle.putCameraState(cameraState: CameraState) {
    with(cameraState) {
        putDouble(CENTER_LONGITUDE, center.longitude())
        putDouble(CENTER_LATITUDE, center.latitude())
        putDouble(PADDING_TOP, padding.top)
        putDouble(PADDING_BOTTOM, padding.bottom)
        putDouble(PADDING_LEFT, padding.left)
        putDouble(PADDING_RIGHT, padding.right)
        putDouble(ZOOM, zoom)
        putDouble(BEARING, bearing)
        putDouble(PITCH, pitch)
    }
}

fun Bundle.containsCameraState(): Boolean =
    listOf(
        CENTER_LONGITUDE,
        CENTER_LATITUDE,
        PADDING_TOP,
        PADDING_BOTTOM,
        PADDING_LEFT,
        PADDING_RIGHT,
        ZOOM,
        BEARING,
        PITCH
    ).all { it in keySet() }

fun Bundle.getCameraOptions(): CameraOptions =
    CameraOptions.Builder()
        .center(
            Point.fromLngLat(
                getDouble(CENTER_LONGITUDE),
                getDouble(CENTER_LATITUDE)
            )
        )
        .padding(
            EdgeInsets(
                getDouble(PADDING_TOP),
                getDouble(PADDING_BOTTOM),
                getDouble(PADDING_LEFT),
                getDouble(PADDING_RIGHT),
            )
        )
        .zoom(getDouble(ZOOM))
        .bearing(getDouble(BEARING))
        .pitch(getDouble(PITCH))
        .build()
