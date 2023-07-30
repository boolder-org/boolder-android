package com.boolder.boolder.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tick(
    @PrimaryKey val id: Int,
)
