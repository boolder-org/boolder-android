package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.AreaDao
import com.boolder.boolder.data.database.entity.AreasEntity
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.AccessFromPoi
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.PoiTransport
import com.boolder.boolder.domain.model.PoiType

class AreaRepository(
    private val areaDao: AreaDao
) {

    suspend fun areasByName(name: String): List<AreasEntity> =
        areaDao.areasByName(name)

    suspend fun getAreaById(id: Int): AreasEntity =
        areaDao.getAreaById(id)

    suspend fun getAllAreas(): List<Area> =
        areaDao.getAllAreas().map { it.convert() }

    suspend fun getAccessesFromPoiByAreaId(areaId: Int): List<AccessFromPoi> =
        areaDao.getAccessesFromPoiByAreaId(areaId).map {
            AccessFromPoi(
                distanceInMinutes = it.distanceInMinutes,
                transport = PoiTransport.fromDbValue(it.transport),
                type = PoiType.fromDbValue(it.poiType),
                name = it.shortName,
                googleUrl = it.googleUrl
            )
        }

    suspend fun getDegreeCountsByArea(areaId: Int): Map<String, Int> {
        val degreeCountsMap = areaDao.getDegreeCountsByArea(areaId)

        return buildMap {
            repeat(8) {
                val degree = (it + 1).toString()

                put(degree, degreeCountsMap.getOrElse(degree) { 0 })
            }
        }
    }
}
