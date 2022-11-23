package com.boolder.boolder.domain.model

import android.os.Parcelable
import com.boolder.boolder.view.detail.PointD
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Parcelize
class Line(
    val id: Int,
    val problemId: Int,
    val topoId: Int,
    val coordinates: String?
) : Parcelable {

    fun coordinatesParsed(): List<Coordinates> {
        val joke = coordinates?.contains("null") == true
        val isNotEmpty = !coordinates.isNullOrBlank()
        return if (isNotEmpty && !joke) {
            Json.decodeFromString(coordinates!!) as List<Coordinates>
        } else emptyList()
    }

    fun points(): List<PointD> {
        // Convert point to Double to avoid loose digit
        return coordinatesParsed().map { PointD(it.x, it.y) }
    }
}