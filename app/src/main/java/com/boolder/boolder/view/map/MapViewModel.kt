package com.boolder.boolder.view.map

import androidx.lifecycle.ViewModel
import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.network.repository.TopoRepository
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.CompleteProblem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MapViewModel(
    private val areaRepository: AreaRepository,
    private val lineRepository: LineRepository,
    private val problemRepository: ProblemRepository,
    private val topoRepository: TopoRepository
) : ViewModel() {
    
    fun fetchProblemAndTopo(problemId: Int): Flow<CompleteProblem> {
        //TODO handle edge(s) case(s)
        return flow {
            val problem: ProblemEntity = problemRepository.loadById(problemId) ?: return@flow
            val line: LineEntity = lineRepository.loadByProblemId(problemId) ?: return@flow
            val topo = topoRepository.getTopoById(line.topoId).getOrNull()?.convert()
            val otherLines: List<LineEntity> = lineRepository.loadAllByTopoIds(line.topoId)
                .filter { it.id != line.id }
            val otherProblems: List<ProblemEntity> = problemRepository.loadAllByIds(otherLines.map { it.problemId })
                .filter { it.id != problemId }

            val others = otherProblems.map { other ->
                CompleteProblem(
                    other.convert(),
                    topo,
                    otherLines.first { it.problemId == other.id }.convert(),
                    emptyList()
                )
            }

            val result = CompleteProblem(
                problem.convert(),
                topo,
                line.convert(),
                others
            )

            emit(result)

        }
    }
}