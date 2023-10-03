package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.ProblemDao
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.entity.ProblemWithAreaName

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
}
