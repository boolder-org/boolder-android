package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.AreaDao
import com.boolder.boolder.data.database.dao.ProblemDao
import com.boolder.boolder.data.database.dao.TickDao
import com.boolder.boolder.data.database.entity.AreaWithProblems
import com.boolder.boolder.data.database.entity.ProblemWithAreaName
import com.boolder.boolder.data.database.entity.TickEntity

class TickRepository(
    private val tickDao: TickDao,
    private val problemDao: ProblemDao,
    private val areaDao: AreaDao,
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

    suspend fun getProblemsPerArea(): List<AreaWithProblems> {
        val tickEntities = tickDao.getAllIds()
        val problemIds = tickEntities.map { it.id }
        val problems = problemDao.loadAllByIds(problemIds)
        val areaWithProblemsList = mutableListOf<AreaWithProblems>()

        val areaIds = problems.map { it.areaId }
        val areas = areaDao.loadAllByIds(areaIds.distinct())

        // Group problems by area ID
        val problemsByAreaId = problems.groupBy { it.areaId }

        for (area in areas) {
            val areaId = area.id
            val problemsForArea = problemsByAreaId[areaId] ?: emptyList()

            areaWithProblemsList.add(AreaWithProblems(area, problemsForArea))
        }

        return areaWithProblemsList
    }

    suspend fun getProblemsByNamePerArea(name: String): List<AreaWithProblems> {
        val tickEntities = tickDao.getAllIds()
        val problemIds = tickEntities.map { it.id }
        val problems = problemDao.getProblemsByIdsAndName(problemIds, name)
        val areaWithProblemsList = mutableListOf<AreaWithProblems>()

        val areaIds = problems.map { it.areaId }
        val areas = areaDao.loadAllByIds(areaIds.distinct())

        // Group problems by area ID
        val problemsByAreaId = problems.groupBy { it.areaId }

        for (area in areas) {
            val areaId = area.id
            val problemsForArea = problemsByAreaId[areaId] ?: emptyList()

            areaWithProblemsList.add(AreaWithProblems(area, problemsForArea))
        }

        return areaWithProblemsList
    }
}