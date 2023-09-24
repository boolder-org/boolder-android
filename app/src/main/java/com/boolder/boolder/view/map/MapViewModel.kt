package com.boolder.boolder.view.map

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.R
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.network.repository.TopoRepository
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.GradeRange
import com.boolder.boolder.domain.model.gradeRangeLevelDisplay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val areaRepository: AreaRepository,
    private val lineRepository: LineRepository,
    private val problemRepository: ProblemRepository,
    private val topoRepository: TopoRepository,
    private val resources: Resources
) : ViewModel() {

    private var currentGradeRange = GradeRange(
        min = ALL_GRADES.first(),
        max = ALL_GRADES.last(),
        isCustom = false
    )

    private val _gradeStateFlow = MutableStateFlow(
        GradeState(
            gradeRangeButtonTitle = resources.getString(R.string.grades),
            grades = ALL_GRADES
        )
    )
    val gradeStateFlow = _gradeStateFlow.asStateFlow()

    private val _areaStateFlow = MutableStateFlow<AreaState>(AreaState.Undefined)
    val areaStateFlow = _areaStateFlow.asStateFlow()

    fun fetchProblemAndTopo(problemId: Int): Flow<CompleteProblem> {
        return flow {
            val mainProblem: ProblemEntity = problemRepository.loadById(problemId) ?: return@flow

            val line = lineRepository.loadByProblemId(problemId)
            val result = if (line != null) {
                val topo = topoRepository.getTopoById(line.topoId).getOrNull()?.convert()
                val otherLines = lineRepository.loadAllByTopoIds(line.topoId)
                    .filter { it.id != line.id }

                val problemsOnSameTopo = problemRepository.loadAllByIds(otherLines.map { it.problemId })
                    .filter {
                        it.id != mainProblem.id
                            && it.parentId == null
                            && it.id != mainProblem.parentId
                    }

                val others = problemsOnSameTopo.map { other ->
                    CompleteProblem(
                        other.convert(),
                        topo,
                        otherLines.first { it.problemId == other.id }.convert(),
                        emptyList()
                    )
                }

                CompleteProblem(
                    mainProblem.convert(),
                    topo,
                    line.convert(),
                    others
                )
            } else CompleteProblem(mainProblem.convert(), null, null, emptyList())

            emit(result)

        }
    }

    fun withCurrentGradeRange(action: (GradeRange) -> Unit) {
        action(currentGradeRange)
    }

    fun onGradeRangeSelected(gradeRange: GradeRange) {
        currentGradeRange = gradeRange

        val grades = with(ALL_GRADES) {
            subList(
                fromIndex = indexOf(gradeRange.min),
                toIndex = indexOf(gradeRange.max) + 1
            )
        }

        val gradeRangeButtonTitle = if (gradeRange == GradeRange.LARGEST) {
            resources.getString(R.string.grades)
        } else {
            resources.gradeRangeLevelDisplay(gradeRange)
        }

        _gradeStateFlow.update {
            GradeState(
                gradeRangeButtonTitle = gradeRangeButtonTitle,
                grades = grades
            )
        }
    }

    fun onAreaVisited(areaId: Int) {
        viewModelScope.launch {
            val currentState = _areaStateFlow.value

            if (currentState is AreaState.Area && currentState.id == areaId) return@launch

            val area = areaRepository.getAreaById(areaId)

            _areaStateFlow.update { AreaState.Area(id = areaId, name = area.name) }
        }
    }

    fun onAreaLeft() {
        if (_areaStateFlow.value is AreaState.Undefined) return

        _areaStateFlow.update { AreaState.Undefined }
    }

    data class GradeState(
        val gradeRangeButtonTitle: String,
        val grades: List<String>
    )

    sealed interface AreaState {
        object Undefined : AreaState

        data class Area(
            val id: Int,
            val name: String
        ) : AreaState
    }
}
