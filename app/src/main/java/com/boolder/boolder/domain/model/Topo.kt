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
 * @param circuitInfo Data related to the circuit that the selected boulder
 * problem belongs to.
 */
data class Topo(
    val pictureUrl: String?,
    val selectedCompleteProblem: CompleteProblem?,
    val otherCompleteProblems: List<CompleteProblem>,
    val circuitInfo: CircuitInfo?,
    val origin: TopoOrigin
)

/**
 * Groups all the data that is related to a circuit.
 *
 * @param color The color of the circuit.
 * @param previousProblemId The ID of the previous boulder problem in the same
 * circuit as the selected boulder problem.
 * @param nextProblemId The ID of the next boulder problem in the same circuit
 * as the selected boulder problem.
 */
data class CircuitInfo(
    val color: CircuitColor,
    val previousProblemId: Int?,
    val nextProblemId: Int?
)

/**
 * The origin of the event that triggered a new topo generation:
 * - MAP corresponds to a click on the map
 * - SEARCH corresponds to a validated search result
 * - TOPO corresponds to a click on the topo's picture
 * - CIRCUIT corresponds to a navigation on the current circuit
 */
enum class TopoOrigin {
    MAP,
    SEARCH,
    TOPO,
    CIRCUIT
}
