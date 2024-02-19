package com.boolder.boolder.data.userdatabase.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ticked_problems")
data class TickedProblemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "problem_id")
    val problemId: Int,
    @ColumnInfo(name = "tick_status")
    val tickStatus: TickStatus
)

enum class TickStatus {
    SUCCEEDED,
    PROJECT
}
