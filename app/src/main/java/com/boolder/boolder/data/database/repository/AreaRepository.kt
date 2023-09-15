package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.AreaDao
import com.boolder.boolder.data.database.entity.AreasEntity


class AreaRepository(
    private val areaDao: AreaDao
) {

    suspend fun areasByName(name: String): List<AreasEntity> =
        areaDao.areasByName(name)

    suspend fun getAreaById(id: Int): AreasEntity =
        areaDao.getAreaById(id)
}

