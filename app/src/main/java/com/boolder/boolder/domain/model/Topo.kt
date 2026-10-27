package com.boolder.boolder.domain.model

/**
 * Represents the content that should be displayed in the boulder problem
 * details bottom sheet.
 *
 * @param photoUri The URI of the picture on which the problem starts and
 * lines will be displayed. Can refer to a web URL or a local file URI.
 * @param selectedCompleteProblem The problem that is currently displayed in
 * the detail information. Its line should also be shown.
 * @param otherCompleteProblems The problems that are on the same picture, but
 * which are not shown in the detail information.
 * @param circuitInfo Data related to the circuit that the selected boulder
 * problem belongs to.
 * @param origin The interaction from which the topo has been requested.
 * @param canShowProblemStarts Indicates if the problem starts and lines can be
 * drawn.
 * @param isPhotoLocked Indicates if the picture should be blurred because it
 * requires a premium subscription.
 * @param premiumPrice The formatted price of the premium subscription, if
 * available.
 * @param topoId The ID of the picture that is displayed.
 * @param boulderTopos The pictures of the same boulder, in display order.
 */
data class Topo(
    val photoUri: String?,
    val selectedCompleteProblem: CompleteProblem?,
    val otherCompleteProblems: List<CompleteProblem>,
    val circuitInfo: CircuitInfo?,
    val origin: TopoOrigin,
    val canShowProblemStarts: Boolean = false,
    val isPhotoLocked: Boolean = false,
    val premiumPrice: String? = null,
    val topoId: Int? = null,
    val boulderTopos: List<BoulderTopo> = emptyList()
)

/**
 * A picture of the boulder on which the selected problem is.
 *
 * @param topoId The ID of the picture.
 * @param photoUri The URI of the picture. Can refer to a web URL or a local
 * file URI.
 */
data class BoulderTopo(
    val topoId: Int,
    val photoUri: String
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
    CIRCUIT,
    DEEP_LINK
}
