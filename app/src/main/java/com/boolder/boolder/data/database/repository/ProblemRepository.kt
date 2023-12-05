package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.ProblemDao
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.entity.ProblemWithAreaName
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.Problem

class ProblemRepository(
    private val problemDao: ProblemDao
) {

    suspend fun loadById(problemId: Int): ProblemEntity? {
        return problemDao.loadById(problemId)
    }

    suspend fun loadAllByIds(problemIds: List<Int>): List<ProblemEntity> {
        return problemDao.loadAllByIds(problemIds)
    }

    suspend fun problemsByName(name: String): List<ProblemWithAreaName> =
        problemDao.problemsByName(name)

    suspend fun problemById(id: Int): ProblemEntity? =
        problemDao.problemById(id)

    suspend fun problemVariantsByParentId(parentProblemId: Int): List<ProblemEntity> =
        problemDao.problemVariantsByParentId(parentProblemId)

    suspend fun problemIdByCircuitAndNumber(
        circuitId: Int,
        circuitProblemNumber: Int
    ): Int? =
        problemDao.problemIdByCircuitAndNumber(circuitId, circuitProblemNumber)

    suspend fun problemsForArea(areaId: Int, query: String = ""): List<Problem> {
        val problems = if (query.isBlank()) {
            problemDao.problemsForArea(areaId)
        } else {
            problemDao.problemsForArea(areaId, query)
        }

        return problems.map { it.convert() }
    }

    suspend fun problemsForCircuit(circuitId: Int): List<Problem> =
        problemDao.problemsForCircuit(circuitId)
            .map { it.convert() }
}
