package com.boolder.boolder.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Index.Order.ASC
import androidx.room.PrimaryKey

@Entity(
    tableName = "problems",
    indices = [
        Index(
            name = "problem_area_idx",
            value = ["area_id"],
            unique = false,
            orders = [ASC]
        ),
        Index(
            name = "problem_circuit_idx",
            value = ["circuit_id"],
            unique = false,
            orders = [ASC]
        ),
        Index(
            name = "problem_idx",
            value = ["id"],
            unique = false,
            orders = [ASC]
        )
    ]
)
data class Problem(
    @PrimaryKey
    val id: Int,
    val name: String?,
    val grade: String?,
    val latitude: Float,
    val longitude: Float,
    @ColumnInfo(name = "circuit_id")
    val circuitId: Int?,
    @ColumnInfo(name = "circuit_number")
    val circuitNumber: String?,
    @ColumnInfo(name = "circuit_color")
    val circuitColor: String?,
    val steepness: String,
    @ColumnInfo(name = "sit_start")
    val sitStart: Boolean,
    @ColumnInfo(name = "area_id")
    val areaId: Int,
    @ColumnInfo(name = "bleau_info_id")
    val bleauInfoId: String?,
    val featured: Boolean,
    @ColumnInfo(name = "parent_id")
    val parentId: Int?
)