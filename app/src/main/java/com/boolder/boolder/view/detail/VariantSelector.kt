package com.boolder.boolder.view.detail

import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.ProblemWithLine

object VariantSelector {

    fun selectVariantInProblemStarts(
        selectedVariant: ProblemWithLine,
        completeProblems: List<CompleteProblem>
    ): Pair<CompleteProblem?, List<CompleteProblem>> {
        var selectedProblem: CompleteProblem? = null

        val newOtherProblems = completeProblems.map { completeProblem ->
            if (completeProblem.problemWithLine == selectedVariant) {
                return@map selectVariantInCompleteProblem(
                    originCompleteProblem = completeProblem,
                    selectedVariant = selectedVariant
                )
                    .also { selectedProblem = it }
            }

            if (completeProblem.variants.any { it == selectedVariant }) {
                return@map selectVariantInCompleteProblem(
                    originCompleteProblem = completeProblem,
                    selectedVariant = selectedVariant
                )
                    .also { selectedProblem = it }
            }

            completeProblem
        }

        return selectedProblem to newOtherProblems
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
                    ?.let { add(it) }

                originCompleteProblem.variants
                    .filter { it != selectedVariant }
                    .let { addAll(it) }
            }
        )
    }
}
