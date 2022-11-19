package com.boolder.boolder.utils

import com.mapbox.maps.extension.style.StyleContract.StyleExtension
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.generated.vectorSource
import com.mapbox.maps.extension.style.style

class MapboxStyleFactory {
    companion object {
        private const val problemsSourceLayerId = "problems-ayes3a" // name of the layer in the mapbox tileset
        private const val styleUri = "mapbox://styles/nmondollot/cl95n147u003k15qry7pvfmq2"
    }

    fun buildStyle(): StyleExtension {
        return style(styleUri) {
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
    }
}