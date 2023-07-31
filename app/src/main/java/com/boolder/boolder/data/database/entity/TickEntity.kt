package com.boolder.boolder.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ticks")
data class TickEntity(
    @PrimaryKey val id: Int,
)
