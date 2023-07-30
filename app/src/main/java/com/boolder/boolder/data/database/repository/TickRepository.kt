package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.ProblemDao
import com.boolder.boolder.data.database.dao.TickDao
import com.boolder.boolder.data.database.entity.ProblemWithAreaName
import com.boolder.boolder.data.database.entity.Tick

class TickRepository(
    private val tickDao: TickDao,
    private val problemDao: ProblemDao
) {

    suspend fun insertTick(tick: Tick) {
        tickDao.insertTick(tick)
    }

    suspend fun loadById(tickId: Int): Tick? {
        return tickDao.loadById(tickId)
    }
    suspend fun getProblemsWithAreaNames(): List<ProblemWithAreaName> {
        val tickEntities = tickDao.getAllIds()
        val problemIds = tickEntities.map { it.id }
        return problemDao.getProblemsWithAreaNamesByIds(problemIds)
    }
}