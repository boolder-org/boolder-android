package com.boolder.boolder.view.map

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import com.boolder.boolder.R
import com.boolder.boolder.data.database.entity.LineEntity
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

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
    val gradeStateFlow: StateFlow<GradeState> = _gradeStateFlow

    fun fetchProblemAndTopo(problemId: Int): Flow<CompleteProblem> {
        return flow {
            val problem: ProblemEntity = problemRepository.loadById(problemId) ?: return@flow
            val line = lineRepository.loadByProblemId(problemId)
            val result = if (line != null) {
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

                CompleteProblem(
                    problem.convert(),
                    topo,
                    line.convert(),
                    others
                )
            } else CompleteProblem(problem.convert(), null, null, emptyList())

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
                toIndex = indexOf(gradeRange.max)
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

    data class GradeState(
        val gradeRangeButtonTitle: String,
        val grades: List<String>
    )
}
