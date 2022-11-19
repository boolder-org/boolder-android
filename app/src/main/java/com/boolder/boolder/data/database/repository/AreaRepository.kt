package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.AreaDao
import com.boolder.boolder.data.database.entity.AreasEntity


class AreaRepository(
    private val areaDao: AreaDao
) {

    suspend fun getAll(): List<AreasEntity> {
        return areaDao.getAll()
    }

    suspend fun loadAllByIds(areaIds: List<Int>): List<AreasEntity> {
        return areaDao.loadAllByIds(areaIds)
    }
}

