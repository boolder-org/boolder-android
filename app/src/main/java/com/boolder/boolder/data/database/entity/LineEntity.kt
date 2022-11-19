package com.boolder.boolder.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Index.Order.ASC
import androidx.room.PrimaryKey

@Entity(
    tableName = "lines",
    indices = [
        Index(
            name = "line_idx",
            value = ["id"],
            unique = false,
            orders = [ASC]
        ),
        Index(
            name = "line_topo_idx",
            value = ["topo_id"],
            unique = false,
            orders = [ASC]
        ),
        Index(
            name = "line_problem_idx",
            value = ["problem_id"],
            unique = false,
            orders = [ASC]
        )
    ]
)
data class LineEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "problem_id")
    val problemId: Int,
    @ColumnInfo(name = "topo_id")
    val topoId: Int,
    val coordinates: String?
)