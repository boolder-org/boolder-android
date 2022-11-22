package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.LineDao
import com.boolder.boolder.data.database.entity.LineEntity

class LineRepository(
    private val lineDao: LineDao
) {

    suspend fun getAll(): List<LineEntity> {
        return lineDao.getAll()
    }

    suspend fun loadAllByTopoIds(topoId: Int): List<LineEntity> {
        return lineDao.loadByTopoId(topoId)
    }

    suspend fun loadByProblemId(problemId: Int): LineEntity? {
        return lineDao.loadByProblemId(problemId)
    }
}