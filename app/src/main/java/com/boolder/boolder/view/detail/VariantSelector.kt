package com.boolder.boolder.view.detail

import androidx.annotation.Px
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.toUiProblem
import com.boolder.boolder.view.detail.uimodel.UiProblem

object VariantSelector {

    fun selectVariantInProblemStarts(
        selectedVariant: ProblemWithLine,
        uiProblems: List<UiProblem>,
        @Px containerWidth: Int,
        @Px containerHeight: Int
    ): Pair<CompleteProblem?, List<UiProblem>> {
        var selectedProblem: CompleteProblem? = null

        val newProblemStarts = uiProblems.mapNotNull { problemStart ->
            val completeProblem = problemStart.completeProblem

            if (completeProblem.problemWithLine == selectedVariant) {
                return@mapNotNull selectVariantInCompleteProblem(
                    originCompleteProblem = completeProblem,
                    selectedVariant = selectedVariant
                )
                    .also { selectedProblem = it }
                    .toUiProblem(
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
                    .toUiProblem(
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
