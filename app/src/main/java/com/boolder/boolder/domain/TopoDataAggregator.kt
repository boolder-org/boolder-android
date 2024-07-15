package com.boolder.boolder.domain

import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.entity.circuitColorSafe
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.userdatabase.repository.TickedProblemRepository
import com.boolder.boolder.domain.model.CircuitInfo
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.TopoOrigin

class TopoDataAggregator(
    private val problemRepository: ProblemRepository,
    private val lineRepository: LineRepository,
    private val tickedProblemRepository: TickedProblemRepository,
    private val photoUriRetriever: PhotoUriRetriever,
    private val circuitProblemsRetriever: CircuitProblemsRetriever
) {

    suspend fun aggregate(
        problemId: Int,
        origin: TopoOrigin,
        previousPhotoUri: String?,
        previousPhotoWasProperlyLoaded: Boolean
    ): Topo {
        val mainProblem = problemRepository.loadById(problemId) ?: return EMPTY_TOPO

        val mainProblemTickStatus = tickedProblemRepository
            .getTickedProblemByProblemId(mainProblem.id)
            ?.tickStatus

        val mainLine = lineRepository.loadByProblemId(problemId)
        val topoId = mainLine?.topoId

        val photoUri = topoId?.let {
            photoUriRetriever.getPhotoUri(topoId = it, areaId = mainProblem.areaId)
        }

        val mainProblemAndLine = ProblemWithLine(
            problem = mainProblem.convert(tickStatus = mainProblemTickStatus),
            line = mainLine?.convert()
        )

        val mainCompleteProblem = getCompleteProblem(mainProblemAndLine)

        val otherCompleteProblems = topoId
            ?.let {
                getOtherCompleteProblemsFromTopo(
                    topoId = topoId,
                    mainProblemWithLine = mainProblemAndLine
                )
            }
            ?: emptyList()

        val (circuitPreviousProblemId, circuitNextProblemId) = circuitProblemsRetriever
            .getCircuitPreviousAndNextProblemIds(mainProblem)

        val hasValidPhoto = previousPhotoUri != null && previousPhotoWasProperlyLoaded
        val canShowProblemStarts = hasValidPhoto && previousPhotoUri == photoUri

        return Topo(
            photoUri = photoUri,
            selectedCompleteProblem = mainCompleteProblem,
            otherCompleteProblems = otherCompleteProblems,
            circuitInfo = CircuitInfo(
                color = mainCompleteProblem.problemWithLine.problem.circuitColorSafe,
                previousProblemId = circuitPreviousProblemId,
                nextProblemId = circuitNextProblemId
            ),
            origin = origin,
            canShowProblemStarts = canShowProblemStarts
        )
    }

    private suspend fun getCompleteProblem(displayedProblemWithLine: ProblemWithLine): CompleteProblem {
        val parentProblemId = displayedProblemWithLine.problem.parentId
            ?: displayedProblemWithLine.problem.id

        val parent = problemRepository
            .problemById(parentProblemId)
            ?.toProblemWithLine()

        val variants = problemRepository
            .problemVariantsByParentId(parentProblemId)
            .map { it.toProblemWithLine() }

        val displayedVariants = buildList {
            if (parent != null && parent.problem.id != displayedProblemWithLine.problem.id) {
                add(parent)
            }

            addAll(variants.filter { it.problem.id != displayedProblemWithLine.problem.id })
        }

        return CompleteProblem(
            problemWithLine = displayedProblemWithLine,
            variants = displayedVariants
        )
    }

    private suspend fun getOtherCompleteProblemsFromTopo(
        topoId: Int,
        mainProblemWithLine: ProblemWithLine
    ): List<CompleteProblem> {
        val (mainProblem, mainLine) = mainProblemWithLine

        val otherLinesFromTopo = lineRepository.loadAllByTopoIds(topoId)
            .filter { it.id != mainLine?.id }

        val problemsOnSameTopo = problemRepository.loadAllByIds(otherLinesFromTopo.map { it.problemId })
            .filter {
                it.id != mainProblem.id
                    && it.parentId == null
                    && it.id != mainProblem.parentId
                    && lineRepository.loadByProblemId(it.id)?.topoId == topoId // todo: clean up once we handle ordering of multiple lines
            }

        val tickStatuses = problemsOnSameTopo.map { problemEntity ->
            tickedProblemRepository
                .getTickedProblemByProblemId(problemEntity.id)
                ?.tickStatus
        }

        val otherProblemsWithLines = problemsOnSameTopo.mapIndexed { index, other ->
            ProblemWithLine(
                problem = other.convert(tickStatus = tickStatuses[index]),
                line = otherLinesFromTopo.first { it.problemId == other.id }.convert()
            )
        }

        return otherProblemsWithLines.map { getCompleteProblem(it) }
    }

    suspend fun updateCircuitControlsForProblem(problemId: Int): CircuitInfo? {
        val problem = problemRepository.problemById(problemId) ?: return null

        val (previousProblemId, nextProblemId) = circuitProblemsRetriever
            .getCircuitPreviousAndNextProblemIds(problem)

        return CircuitInfo(
            color = problem.circuitColorSafe,
            previousProblemId = previousProblemId,
            nextProblemId = nextProblemId
        )
    }

    private suspend fun ProblemEntity.toProblemWithLine(): ProblemWithLine {
        val problem = this.convert()

        return ProblemWithLine(
            problem = problem,
            line = lineRepository.loadByProblemId(problem.id)?.convert()
        )
    }

    companion object {
        private val EMPTY_TOPO = Topo(
            photoUri = null,
            selectedCompleteProblem = null,
            otherCompleteProblems = emptyList(),
            circuitInfo = null,
            origin = TopoOrigin.MAP
        )
    }
}
