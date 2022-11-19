package com.boolder.boolder.database.repository

import com.boolder.boolder.database.dao.AreaDao
import com.boolder.boolder.database.entity.Areas


class AreaRepository(
    private val areaDao: AreaDao
) {

    suspend fun getAll(): List<Areas> {
        return areaDao.getAll()
    }

    suspend fun loadAllByIds(areaIds: List<Int>): List<Areas> {
        return areaDao.loadAllByIds(areaIds)
    }
}

