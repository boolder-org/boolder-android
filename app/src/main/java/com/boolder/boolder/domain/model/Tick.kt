package com.boolder.boolder.domain.model

import android.os.Parcelable
import com.boolder.boolder.view.search.BaseObject
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tick(
    val id: Int,
) : BaseObject, Parcelable
