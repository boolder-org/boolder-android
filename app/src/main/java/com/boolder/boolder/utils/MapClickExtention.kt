package com.boolder.boolder.utils

import android.util.Log
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo

//TODO Doc these extensions

fun MapboxMap.queryAreaRenderedFeatures(geometry: RenderedQueryGeometry, callback: (CoordinateBounds) -> Unit) {
    val areaOption = RenderedQueryOptions(
        listOf("areas", "areas-hulls"),
        Expression.lt(
            Expression.zoom(),
            Expression.literal(15.0)
        )
    )
    queryRenderedFeatures(geometry, areaOption) { features: Expected<String, MutableList<QueriedFeature>> ->
        buildCoordinateBounds(features, callback)
    }
}

fun MapboxMap.queryClusterRenderedFeatures(geometry: RenderedQueryGeometry, callback: (CoordinateBounds) -> Unit) {
    val clusterOption = RenderedQueryOptions(listOf("cluster"), null)

    queryRenderedFeatures(geometry, clusterOption) { features: Expected<String, MutableList<QueriedFeature>> ->
        buildCoordinateBounds(features, callback)
    }
}

fun MapboxMap.queryPoisRenderedFeatures(
    geometry: RenderedQueryGeometry,
    callback: (String, String, Geometry?) -> Unit
) {
    val poisOption = RenderedQueryOptions(listOf("pois"), null)
    queryRenderedFeatures(geometry, poisOption) { features: Expected<String, MutableList<QueriedFeature>> ->
        if (features.isValue) {
            features.value?.firstOrNull()?.feature?.let {
                if (it.hasProperty("name") &&
                    it.hasProperty("googleUrl")
                ) {
                    callback(
                        it.getStringProperty("name"),
                        it.getStringProperty("googleUrl"),
                        it.geometry()
                    )
                }
            }
        } else {
            Log.w("MAP LAYERS", features.error ?: "No message")
        }
    }
}

fun MapboxMap.queryProblemRenderedFeatures(
    geometry: RenderedQueryGeometry,
    screenHeight: Int,
    callback: (Int) -> Unit
) {

    val problemGeometry = RenderedQueryGeometry(
        ScreenBox(
            ScreenCoordinate(
                geometry.screenCoordinate.x - 12.0, geometry.screenCoordinate.y - 12.0
            ),
            ScreenCoordinate(
                geometry.screenCoordinate.x + 12.0,
                geometry.screenCoordinate.y + 12.0
            )
        )
    )

    val problemsOption = RenderedQueryOptions(listOf("problems"), null)

    queryRenderedFeatures(
        problemGeometry,
        problemsOption
    ) { features: Expected<String, MutableList<QueriedFeature>> ->
        if (features.isValue) {
            features.value?.firstOrNull()?.feature?.let {
                if (it.hasProperty("id") && it.geometry() != null) {
                    callback(it.getNumberProperty("id").toInt())

                    // Move camera is problem is hidden by bottomSheet
                    if (geometry.screenCoordinate.y >= screenHeight) {

                        val cameraOption = CameraOptions.Builder()
                            .center(it.geometry() as Point)
                            .padding(EdgeInsets(60.0, 8.8, (screenHeight).toDouble(), 8.8))
                            .build()
                        val mapAnimationOption = MapAnimationOptions.Builder()
                            .duration(500L)
                            .build()

                        easeTo(cameraOption, mapAnimationOption)
                    }
                }
            }
        } else {
            Log.w("MAP LAYERS", features.error ?: "No message")
        }
    }
}


// 3A. Build bounds around coordinate
private fun buildCoordinateBounds(
    features: Expected<String, MutableList<QueriedFeature>>,
    callback: (CoordinateBounds) -> Unit
) {
    if (features.isValue) {
        features.value?.firstOrNull()?.feature?.let {
            if (it.hasProperty("southWestLon") &&
                it.hasProperty("southWestLat") &&
                it.hasProperty("northEastLon") &&
                it.hasProperty("northEastLat")
            ) {
                val southWest = Point.fromLngLat(
                    it.getStringProperty("southWestLon").toDouble(),
                    it.getStringProperty("southWestLat").toDouble()
                )
                val northEst = Point.fromLngLat(
                    it.getStringProperty("northEastLon").toDouble(),
                    it.getStringProperty("northEastLat").toDouble()
                )
                val coordinateBound = CoordinateBounds(southWest, northEst)
                callback(coordinateBound)
            }
        }
    } else {
        Log.w("MAP LAYER", features.error ?: "No message")
    }
}