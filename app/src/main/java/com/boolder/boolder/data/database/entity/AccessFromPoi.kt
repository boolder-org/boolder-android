package com.boolder.boolder.data.database.entity

data class AccessFromPoi(
    val distanceInMinutes: Int,
    val transport: String,
    val poiType: String,
    val shortName: String,
    val googleUrl: String
)
