package com.boolder.boolder.view.detail.uimodel

import androidx.annotation.ColorRes
import com.boolder.boolder.domain.model.CompleteProblem

data class ProblemStart(
    val x: Int,
    val y: Int,
    val dpSize: Int,
    @ColorRes val colorRes: Int,
    @ColorRes val textColorRes: Int,
    val completeProblem: CompleteProblem
)
