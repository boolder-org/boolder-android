package com.boolder.boolder.domain.model

import androidx.annotation.Px
import com.boolder.boolder.view.detail.uimodel.ProblemStart
import kotlin.math.roundToInt

/**
 * Wrapper for a boulder problem, along with its variants
 */
data class CompleteProblem(
    val problemWithLine: ProblemWithLine,
    val variants: List<ProblemWithLine>
)

fun CompleteProblem.toProblemStart(
    @Px containerWidthPx: Int,
    @Px containerHeightPx: Int
): ProblemStart? {
    val startPoint = problemWithLine.line?.points()?.firstOrNull() ?: return null

    val problem = problemWithLine.problem

    return ProblemStart(
        x = (startPoint.x * containerWidthPx).roundToInt(),
        y = (startPoint.y * containerHeightPx).roundToInt(),
        dpSize = if (problem.circuitNumber.isNullOrBlank()) 16 else 28,
        colorRes = problem.circuitColorSafe.colorRes,
        textColorRes = if (problem.circuitColorSafe == CircuitColor.WHITE) {
            android.R.color.black
        } else {
            android.R.color.white
        },
        completeProblem = this
    )
}
