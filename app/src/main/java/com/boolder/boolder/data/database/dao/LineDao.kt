package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.LineEntity

@Dao
interface LineDao {

    @Query("SELECT * FROM lines")
    suspend fun getAll(): List<LineEntity>

    @Query("SELECT * FROM lines WHERE id IN (:lineIds)")
    suspend fun loadAllByIds(lineIds: List<Int>): List<LineEntity>
}