package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.ProblemEntity

@Dao
interface ProblemDao {

    @Query("SELECT * FROM problems")
    suspend fun getAll(): List<ProblemEntity>

    @Query("SELECT * FROM problems WHERE id == :problemId")
    suspend fun loadById(problemId: Int): ProblemEntity?
}