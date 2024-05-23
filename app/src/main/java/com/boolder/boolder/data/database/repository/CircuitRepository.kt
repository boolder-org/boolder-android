package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.CircuitDao
import com.boolder.boolder.data.database.entity.CircuitEntity
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.CircuitColor
import com.mapbox.geojson.Point

class CircuitRepository(
    private val circuitDao: CircuitDao
) {

    suspend fun getCircuitById(circuitId: Int): Circuit? =
        circuitDao.getCircuitById(circuitId)?.toCircuit()

    suspend fun getAvailableCircuits(areaId: Int): List<Circuit> =
        circuitDao.getAvailableCircuits(areaId = areaId)
            .map { it.toCircuit() }

    suspend fun getBeginnerFriendlyCircuits(areaId: Int): List<Circuit> =
        circuitDao.getBeginnerFriendlyCircuits(areaId = areaId)
            .map { it.toCircuit() }

    suspend fun getCircuitFromProblemId(problemId: Int): Circuit? =
        circuitDao.getCircuitFromProblemId(problemId)?.toCircuit()

    private fun CircuitEntity.toCircuit() = Circuit(
        id = id,
        color = try {
            CircuitColor.valueOf(color.uppercase())
        } catch (e: Exception) {
            CircuitColor.OFF_CIRCUIT
        },
        averageGrade = averageGrade,
        isBeginnerFriendly = beginnerFriendly,
        isDangerous = dangerous,
        coordinateBounds = listOf(
            Point.fromLngLat(southWestLng, southWestLat),
            Point.fromLngLat(northEastLng, northEastLat)
        )
    )
}
