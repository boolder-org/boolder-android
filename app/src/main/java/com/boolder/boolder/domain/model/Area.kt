package com.boolder.boolder.domain.model

import com.boolder.boolder.view.search.BaseObject

data class Area(
    val id: Int,
    val name: String,
    val southWestLat: Float,
    val southWestLon: Float,
    val northEastLat: Float,
    val northEastLon: Float
) : BaseObject
