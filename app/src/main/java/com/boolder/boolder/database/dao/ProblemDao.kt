package com.boolder.boolder.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.database.entity.Problem

@Dao
interface ProblemDao {

    @Query("SELECT * FROM problems")
    suspend fun getAll(): List<Problem>

    @Query("SELECT * FROM problems WHERE id IN (:problemIds)")
    suspend fun loadAllByIds(problemIds: List<Int>): List<Problem>
}