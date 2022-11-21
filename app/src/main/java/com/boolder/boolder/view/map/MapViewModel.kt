package com.boolder.boolder.view.map

import androidx.lifecycle.ViewModel
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
        return flow {
            lineRepository.loadByProblemId(problemId)?.let {
                val problem = problemRepository.loadById(problemId)
                val topo = topoRepository.getTopoById(it.id)
                if (problem != null && (topo.isSuccess && topo.getOrNull() != null)) {
                    emit(CompleteProblem(problem.convert(), topo.getOrNull()!!.convert(), it.convert()))
                } else {
                    //TODO handle edge(s) case(s)
                }
            }
        }
    }
}