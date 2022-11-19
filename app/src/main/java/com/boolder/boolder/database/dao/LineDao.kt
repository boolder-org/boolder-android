package com.boolder.boolder.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.database.entity.Line

@Dao
interface LineDao {

    @Query("SELECT * FROM lines")
    suspend fun getAll(): List<Line>

    @Query("SELECT * FROM lines WHERE id IN (:lineIds)")
    suspend fun loadAllByIds(lineIds: List<Int>): List<Line>
}