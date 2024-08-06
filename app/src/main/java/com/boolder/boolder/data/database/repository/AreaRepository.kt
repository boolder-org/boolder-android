package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.AreaDao
import com.boolder.boolder.data.database.entity.AreaEntity
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.AccessFromPoi
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.AreaBikeRoute
import com.boolder.boolder.domain.model.PoiTransport
import com.boolder.boolder.domain.model.PoiType
import com.boolder.boolder.domain.model.TrainStationPoi

class AreaRepository(
    private val areaDao: AreaDao
) {

    suspend fun areasByName(name: String): List<AreaEntity> =
        areaDao.areasByName(name)

    suspend fun getAreaById(id: Int): Area? =
        areaDao.getAreaById(id)?.convert()

    suspend fun getAllAreas(): List<Area> =
        areaDao.getAllAreas().map { it.convert() }

    suspend fun getAllAreasByProblemsCount(): List<Area> =
        areaDao.getAllAreasByProblemsCount().map { it.convert() }

    suspend fun getAllBeginnerFriendlyAreas(): List<Area> =
        areaDao.getAllBeginnerFriendlyAreas().map { it.areaEntity.convert() }

    suspend fun getTaggedAreas(tag: String): List<Area> =
        areaDao.getTaggedAreas(tag).map { it.convert() }

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

    suspend fun getTrainStationPoiWithBikeRoutes(): Map<TrainStationPoi, List<AreaBikeRoute>> =
        areaDao.getTrainStationPoiWithBikeRoutes().groupBy(
            keySelector = {
                TrainStationPoi(
                    name = it.trainStationName,
                    googleUrl = it.googleUrl
                )
            },
            valueTransform = {
                AreaBikeRoute(
                    areaId = it.areaId,
                    areaName = it.areaName,
                    bikingTime = it.bikingTime
                )
            }
        )

    suspend fun getAllTopoIdsForArea(areaId: Int): List<Int> =
        areaDao.getAllTopoIdsForArea(areaId)
}
