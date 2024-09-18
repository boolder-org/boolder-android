package com.boolder.boolder.domain.model

import android.os.Parcelable
import com.boolder.boolder.view.search.BaseObject
import kotlinx.parcelize.Parcelize
import kotlin.math.pow
import kotlin.math.sqrt

@Parcelize
data class Area(
    val id: Int,
    val name: String,
    val description: String? = null,
    val warning: String? = null,
    val tags: List<Int> = emptyList(),
    val southWestLat: Float,
    val southWestLon: Float,
    val northEastLat: Float,
    val northEastLon: Float,
    val problemsCount: Int,
    val problemsCountsPerGrade: Map<String, Int>
) : BaseObject, Parcelable

fun Area.center(): Pair<Float, Float> {
    val centerLatitude = (northEastLat + southWestLat) / 2
    val centerLongitude = (northEastLon + southWestLon) / 2

    return Pair(centerLatitude, centerLongitude)
}

fun Area.containsPoint(latitude: Double, longitude: Double): Boolean =
    northEastLat < latitude
        && southWestLat > latitude
        && northEastLon < longitude
        && southWestLon > longitude

fun Area.centerDistanceFromLatLon(latitude: Double, longitude: Double): Double {
    val (centerLatitude, centerLongitude) = center()

    return sqrt((centerLatitude - latitude).pow(2) + (centerLongitude - longitude).pow(2))
}
