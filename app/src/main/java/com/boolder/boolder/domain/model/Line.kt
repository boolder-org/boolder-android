package com.boolder.boolder.domain.model

import android.os.Parcelable
import com.boolder.boolder.view.detail.PointD
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.json.Json

@Parcelize
data class Line(
    val id: Int,
    val problemId: Int,
    val topoId: Int,
    val coordinates: String?
) : Parcelable {

    fun points(): List<PointD> {
        // Convert point to Double to avoid loose digit
        return decodedCoordinates().map { PointD(it.x, it.y) }
    }

    private fun decodedCoordinates(): List<Coordinates> {
        if (coordinates.isNullOrBlank()) return emptyList()
        if ("null" in coordinates) return emptyList()

        return Json.decodeFromString(coordinates) as List<Coordinates>
    }
}
