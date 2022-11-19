package com.boolder.boolder.utils

import android.util.Log
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.generated.vectorSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo

private const val problemsSourceLayerId = "problems-ayes3a" // name of the layer in the mapbox tileset
private const val styleUri = "mapbox://styles/nmondollot/cl95n147u003k15qry7pvfmq2"

fun MapboxMap.loadBoolderLayer() {
    loadStyle(
        style(styleUri) {
            +vectorSource("problems") {
                url("mapbox://nmondollot.4xsv235p")
            }
            +circleLayer("problems", "problems") {
                sourceLayer(problemsSourceLayerId)
                minZoom(15.0)
                filter(Expression.match {
                    geometryType()
                    get("Point")
                    literal(true)
                    literal(false)
                })

                circleRadius(
                    interpolate {
                        linear()
                        zoom()
                        literal(15.0)
                        literal(2.0)
                        literal(18.0)
                        literal(4.0)
                        literal(22.0)
                        switchCase {
                            boolean {
                                has("circuitColor")
                                literal(false)
                            }
                            literal(16.0)
                            literal(10.0)
                        }
                    }
                )

                circleColor(
                    match {
                        get { literal("circuitColor") }
                        literal("yellow")
                        rgb(255.0, 204.0, 2.0)
                        literal("purple")
                        rgb(215.0, 131.0, 255.0)
                        literal("orange")
                        rgb(255.0, 149.0, 0.0)
                        literal("green")
                        rgb(255.0, 149.0, 0.0)
                        literal("blue")
                        rgb(1.0, 122.0, 255.0)
                        literal("skyblue")
                        rgb(90.0, 199.0, 250.0)
                        literal("salmon")
                        rgb(253.0, 175.0, 138.0)
                        literal("red")
                        rgb(255.0, 59.0, 47.0)
                        literal("black")
                        rgb(0.0, 0.0, 0.0)
                        literal("white")
                        rgb(255.0, 255.0, 255.0)
                        rgb(135.0, 138.0, 141.0)
                    }
                )

                circleStrokeWidth(
                    Expression.switchCase {
                        boolean {
                            featureState { literal("selected") }
                            literal(false)
                        }
                        literal(3.0)
                        literal(0.0)
                    }
                )

                circleStrokeColor(rgb(101.0, 196.0, 102.0))
                circleSortKey(
                    switchCase {
                        boolean {
                            has { literal("circuitId") }
                            literal(false)
                        }
                        literal(2)
                        literal(1)
                    }

                )
            }
            +symbolLayer("problems-text", "problems") {
                sourceLayer(problemsSourceLayerId)
                minZoom(19.0)
                filter(Expression.match {
                    geometryType()
                    get("Point")
                    literal(true)
                    literal(false)
                })
                textAllowOverlap(true)
                textField(
                    get("circuitNumber")
                )
                textSize(interpolate {
                    linear()
                    zoom()
                    literal(19)
                    literal(10)
                    literal(22)
                    literal(20)
                })
                textColor(switchCase {
                    match {
                        get("circuitColor")
                        literal("white")
                        literal(true)
                        literal(false)
                    }
                    rgb(0.0, 0.0, 0.0)
                    rgb(255.0, 255.0, 255.0)
                })
            }
        }
    )
}

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
    geometry.screenCoordinate.y

    val problemGeometry = RenderedQueryGeometry(
        ScreenCoordinate(geometry.screenCoordinate.x - 12.0, geometry.screenCoordinate.y - 12.0)
    )
    val problemsOption = RenderedQueryOptions(listOf("problems"), null)

    queryRenderedFeatures(
        problemGeometry,
        problemsOption
    ) { features: Expected<String, MutableList<QueriedFeature>> ->
        if (features.isValue) {
            println("PROBLEMS ${features.value}")
            features.value?.firstOrNull()?.feature?.let {
                println("PROBLEM $it")
                if (it.hasProperty("id") && it.geometry() != null) {
                    callback(it.getNumberProperty("id").toInt())

                    println("Will be hidden ${geometry.screenCoordinate.y >= screenHeight}")
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