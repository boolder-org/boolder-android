package com.boolder.boolder.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topos")
data class TopoEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "area_id")
    val areaId: Int,
    @ColumnInfo(name = "boulder_id")
    val boulderId: Int?,
    val position: Int?
)
