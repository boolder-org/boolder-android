package com.boolder.boolder.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "areas",
    indices = [Index(
        name = "area_idx",
        value = ["id"],
        unique = false,
        orders = [Index.Order.ASC]
    )]
)
data class Areas(
    @PrimaryKey
    val id: Int,
    val name: String,
    @ColumnInfo(name = "south_west_lat")
    val southWestLat: Float,
    @ColumnInfo(name = "south_west_lon")
    val southWestLon: Float,
    @ColumnInfo(name = "north_east_lat")
    val northEastLat: Float,
    @ColumnInfo(name = "north_east_lon")
    val northEastLon: Float
)

