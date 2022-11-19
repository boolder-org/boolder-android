package com.boolder.boolder.database.repository

import com.boolder.boolder.database.dao.LineDao
import com.boolder.boolder.database.entity.Line

class LineRepository(
    private val lineDao: LineDao
) {

    suspend fun getAll(): List<Line> {
        return lineDao.getAll()
    }

    suspend fun loadAllByIds(lineIds: List<Int>): List<Line> {
        return lineDao.loadAllByIds(lineIds)
    }
}