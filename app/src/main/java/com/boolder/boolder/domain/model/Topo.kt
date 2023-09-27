package com.boolder.boolder.domain.model

/**
 * Represents the content that should be displayed in the boulder problem
 * details bottom sheet.
 *
 * @param pictureUrl The URL of the picture on which the problem starts and
 * lines will be displayed.
 * @param selectedCompleteProblem The problem that is currently displayed in
 * the detail information. Its line should also be shown.
 * @param otherCompleteProblems The problems that are on the same picture, but
 * which are not shown in the detail information.
 */
data class Topo(
    val pictureUrl: String?,
    val selectedCompleteProblem: CompleteProblem?,
    val otherCompleteProblems: List<CompleteProblem>
)
