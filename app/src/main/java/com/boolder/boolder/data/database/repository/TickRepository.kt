package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.ProblemDao
import com.boolder.boolder.data.database.dao.TickDao
import com.boolder.boolder.data.database.entity.ProblemWithAreaName
import com.boolder.boolder.data.database.entity.TickEntity
import com.boolder.boolder.domain.model.Tick

class TickRepository(
    private val tickDao: TickDao,
    private val problemDao: ProblemDao
) {

    suspend fun insertTick(tick: TickEntity) {
        tickDao.insertTick(tick)
    }

    suspend fun loadById(tickId: Int): TickEntity? {
        return tickDao.loadById(tickId)
    }

    suspend fun deleteById(tickId: Int){
        return tickDao.deleteById(tickId)
    }

    suspend fun deleteAll(){
        return tickDao.deleteAll()
    }
    suspend fun getProblemsWithAreaNames(): List<ProblemWithAreaName> {
        val tickEntities = tickDao.getAllIds()
        val problemIds = tickEntities.map { it.id }
        return problemDao.getProblemsWithAreaNamesByIds(problemIds)
    }

    suspend fun getProblemsWithAreaNamesByName(name: String): List<ProblemWithAreaName> {
        val tickEntities = tickDao.getAllIds()
        val problemIds = tickEntities.map { it.id }
        return problemDao.getProblemsWithAreaNamesByIdsAndName(problemIds, name)
    }
}