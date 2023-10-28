package com.boolder.boolder.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "circuits")
data class CircuitEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "color")
    val color: String,

    @ColumnInfo(name = "average_grade")
    val averageGrade: String,

    @ColumnInfo(name = "beginner_friendly")
    val beginnerFriendly: Boolean,

    @ColumnInfo(name = "dangerous")
    val dangerous: Boolean,

    @ColumnInfo(name = "south_west_lat")
    val southWestLat: Double,

    @ColumnInfo(name = "south_west_lon")
    val southWestLng: Double,

    @ColumnInfo(name = "north_east_lat")
    val northEastLat: Double,

    @ColumnInfo(name = "north_east_lon")
    val northEastLng: Double
)
