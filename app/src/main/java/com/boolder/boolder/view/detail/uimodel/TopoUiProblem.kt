package com.boolder.boolder.view.detail.uimodel

import com.boolder.boolder.domain.model.CompleteProblem

/**
 * Container for a [CompleteProblem] and its associated UI data to render a
 * boulder problem start marker.
 *
 * @param completeProblem the boulder problem with its variants
 * @param problemStart the associated UI data to render a boulder problem start
 * marker
 */
data class UiProblem(
    val completeProblem: CompleteProblem,
    val problemStart: ProblemStart?
)
