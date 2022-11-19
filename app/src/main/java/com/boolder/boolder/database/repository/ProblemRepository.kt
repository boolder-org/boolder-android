package com.boolder.boolder.database.repository

import com.boolder.boolder.database.dao.ProblemDao
import com.boolder.boolder.database.entity.Problem

class ProblemRepository(
    private val problemDao: ProblemDao
) {

    suspend fun getAll(): List<Problem> {
        return problemDao.getAll()
    }

    suspend fun loadAllByIds(problemIds: List<Int>): List<Problem> {
        return problemDao.loadAllByIds(problemIds)
    }
}