package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.LineEntity

@Dao
interface LineDao {

    @Query("SELECT * FROM lines WHERE topo_id = :topoId")
    suspend fun loadByTopoId(topoId: Int): List<LineEntity>

    @Query("SELECT * FROM lines WHERE problem_id = :problemId")
    suspend fun loadByProblemId(problemId: Int): LineEntity?
}
