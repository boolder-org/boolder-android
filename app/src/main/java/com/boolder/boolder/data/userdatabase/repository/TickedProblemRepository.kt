package com.boolder.boolder.data.userdatabase.repository

import com.boolder.boolder.data.userdatabase.dao.TickedProblemDao
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.TickedProblem

class TickedProblemRepository(
    private val tickedProblemDao: TickedProblemDao
) {

    suspend fun insertTickedProblem(tickedProblem: TickedProblem) {
        tickedProblemDao.insertTickedProblem(tickedProblem.toEntity())
    }

    suspend fun getAllTickedProblems(): List<TickedProblem> =
        tickedProblemDao.getAllTickedProblems().map { it.convert() }

    suspend fun getTickedProblemByProblemId(problemId: Int): TickedProblem? =
        tickedProblemDao.getTickedProblemByProblemId(problemId)?.convert()

    suspend fun deleteTickedProblemByProblemId(problemId: Int) {
        tickedProblemDao.deleteTickedProblemByProblemId(problemId)
    }
}
