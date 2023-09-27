package com.boolder.boolder.domain.model

import android.os.Parcelable
import androidx.annotation.Px
import com.boolder.boolder.view.detail.uimodel.ProblemStart
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class CompleteProblem(
    val problem: Problem,
    val topo: Topo?,
    val line: Line?,
    val otherCompleteProblem: List<CompleteProblem>
) : Parcelable

fun CompleteProblem.toProblemStart(
    @Px containerWidthPx: Int,
    @Px containerHeightPx: Int
): ProblemStart? {
    val startPoint = line?.points()?.firstOrNull() ?: return null

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
