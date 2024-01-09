package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.CircuitColor
import com.mapbox.geojson.Point
import com.mapbox.maps.CoordinateBounds

fun dummyCircuit(
    id: Int = 0,
    color: CircuitColor = CircuitColor.ORANGE,
    averageGrade: String = "3b"
) = Circuit(
    id = id,
    color = color,
    averageGrade = averageGrade,
    isBeginnerFriendly = false,
    isDangerous = false,
    coordinateBounds = CoordinateBounds(
        Point.fromLngLat(0.0, 0.0),
        Point.fromLngLat(0.0, 0.0)
    )
)
