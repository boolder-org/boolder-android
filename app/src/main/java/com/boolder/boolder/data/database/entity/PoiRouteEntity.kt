package com.boolder.boolder.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poi_routes")
data class PoiRouteEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo("area_id") val areaId: Int,
    @ColumnInfo("poi_id") val poiId: Int,
    @ColumnInfo("distance_in_minutes") val distanceInMinutes: Int,
    @ColumnInfo("transport") val transport: String
)
