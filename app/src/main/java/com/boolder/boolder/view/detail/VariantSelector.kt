package com.boolder.boolder.view.detail

import androidx.annotation.Px
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.toProblemStart
import com.boolder.boolder.view.detail.uimodel.ProblemStart

object VariantSelector {

    fun selectVariantInProblemStarts(
        selectedVariant: ProblemWithLine,
        problemStarts: List<ProblemStart>,
        @Px containerWidth: Int,
        @Px containerHeight: Int
    ): Pair<CompleteProblem?, List<ProblemStart>> {
        var selectedProblem: CompleteProblem? = null

        val newProblemStarts = problemStarts.mapNotNull { problemStart ->
            val completeProblem = problemStart.completeProblem

            if (completeProblem.problemWithLine == selectedVariant) {
                return@mapNotNull selectVariantInCompleteProblem(
                    originCompleteProblem = completeProblem,
                    selectedVariant = selectedVariant
                )
                    .also { selectedProblem = it }
                    .toProblemStart(
                        containerWidthPx = containerWidth,
                        containerHeightPx = containerHeight
                    )
            }

            if (completeProblem.variants.any { it == selectedVariant }) {
                return@mapNotNull selectVariantInCompleteProblem(
                    originCompleteProblem = completeProblem,
                    selectedVariant = selectedVariant
                )
                    .also { selectedProblem = it }
                    .toProblemStart(
                        containerWidthPx = containerWidth,
                        containerHeightPx = containerHeight
                    )
            }

            problemStart
        }

        return selectedProblem to newProblemStarts
    }

    private fun selectVariantInCompleteProblem(
        originCompleteProblem: CompleteProblem,
        selectedVariant: ProblemWithLine
    ): CompleteProblem {
        return CompleteProblem(
            problemWithLine = selectedVariant,
            variants = buildList {
                originCompleteProblem.problemWithLine
                    .takeIf { it != selectedVariant }
                    ?.let(::add)

                originCompleteProblem.variants
                    .filter { it != selectedVariant }
                    .let(::addAll)
            }
        )
    }
}
