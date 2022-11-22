package com.boolder.boolder.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CompleteProblem(
    val problem: Problem,
    val topo: Topo?,
    val line: Line,
    val otherCompleteProblem: List<CompleteProblem>
) : Parcelable
