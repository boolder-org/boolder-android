package com.boolder.boolder.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pois")
data class PoiEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo("poi_type") val poiType: String,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("short_name") val shortName: String,
    @ColumnInfo("google_url") val googleMapsUrl: String
)
