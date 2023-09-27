package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.view.detail.uimodel.ProblemStart

fun dummyProblemStart(
    x: Int,
    y: Int,
    completeProblem: CompleteProblem = dummyCompleteProblem()
) = ProblemStart(
    x = x,
    y = y,
    dpSize = 28,
    colorRes = CircuitColor.RED.colorRes,
    textColorRes = android.R.color.white,
    completeProblem = completeProblem
)
