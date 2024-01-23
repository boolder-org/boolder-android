package com.boolder.boolder.domain.model

data class TrainStationPoiWithBikeRoutes(
    val trainStationName: String,
    val googleUrl: String,
    val areaId: Int,
    val areaName: String,
    val bikingTime: Int
)
