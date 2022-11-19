package com.boolder.boolder.view.map

import androidx.lifecycle.ViewModel
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.network.repository.TopoRepository
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Topo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class MapViewModel(
    private val areaRepository: AreaRepository,
    private val lineRepository: LineRepository,
    private val problemRepository: ProblemRepository,
    private val topoRepository: TopoRepository
) : ViewModel() {

    fun getProblemById(problemId: Int): Flow<Problem?> {
        return flow {
            val result = problemRepository.loadById(problemId)?.convert()
            emit(result)
        }
    }

    fun getTopoById(problemId: Int): Flow<Topo?> {
        return flow {
            val result = topoRepository.getTopoById(problemId).map { it.convert() }
            if (result.isSuccess) {
                emit(result.getOrNull())
            } else {
                //TODO Log a message
            }
        }
    }

    fun getProblemAndTopo(problemId: Int): Flow<Pair<Topo?, Problem?>> {
        val topoFlow = getTopoById(1234) // TODO Need to know how to get this id
        val problemFlow = getProblemById(problemId)
        return topoFlow.combine(problemFlow) { topo, problem -> Pair(topo, problem) }
    }
}