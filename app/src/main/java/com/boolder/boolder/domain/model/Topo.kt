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
 * @param circuitPreviousProblemId The ID of the previous boulder problem in
 * the same circuit as the selected boulder problem.
 * @param circuitNextProblemId The ID of the next boulder problem in the same
 * circuit as the selected boulder problem.
 */
data class Topo(
    val pictureUrl: String?,
    val selectedCompleteProblem: CompleteProblem?,
    val otherCompleteProblems: List<CompleteProblem>,
    val circuitPreviousProblemId: Int?,
    val circuitNextProblemId: Int?
)
