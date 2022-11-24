package com.boolder.boolder.domain.model

import android.os.Parcelable
import com.boolder.boolder.view.search.BaseObject
import kotlinx.parcelize.Parcelize

@Parcelize
data class Area(
    val id: Int,
    val name: String,
    val southWestLat: Float,
    val southWestLon: Float,
    val northEastLat: Float,
    val northEastLon: Float
) : BaseObject, Parcelable
