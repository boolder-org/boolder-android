package com.boolder.boolder.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Line(
    val id: Int,
    val problemId: Int,
    val topoId: Int,
    val coordinates: String?
) : Parcelable