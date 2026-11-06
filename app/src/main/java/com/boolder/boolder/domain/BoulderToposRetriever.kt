package com.boolder.boolder.domain

import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.repository.BoulderTopoRepository
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository

class BoulderToposRetriever(
    private val boulderTopoRepository: BoulderTopoRepository,
    private val lineRepository: LineRepository,
    private val problemRepository: ProblemRepository
) {

    suspend fun getBoulderTopoIds(topoId: Int): List<Int> {
        val boulderId = boulderTopoRepository.loadById(topoId)?.boulderId
            ?: return listOf(topoId)

        return boulderTopoRepository.loadAllByBoulderId(boulderId).map { it.id }
    }

    suspend fun getTopProblemId(topoId: Int): Int? {
        // a problem with lines on several topos is shown on the topo of its first line only
        val lines = lineRepository.loadAllByTopoIds(topoId)
            .filter { lineRepository.loadByProblemId(it.problemId)?.topoId == topoId }
        val problemIdsWithLine = lines.filter { it.convert().points().isNotEmpty() }.map { it.problemId }
        val problems = problemRepository.loadAllByIds(lines.map { it.problemId })

        val topProblem = problems.filter { it.id in problemIdsWithLine }.maxByOrNull { it.zIndex() }
            ?: problems.maxByOrNull { it.zIndex() }

        return topProblem?.id
    }
}

private fun ProblemEntity.zIndex(): Double {
    val circuitBonus = if (circuitId != null) 1000.0 else 0.0
    val tiebreaker = id / 100.0

    return (popularity ?: 0) + circuitBonus + tiebreaker
}
