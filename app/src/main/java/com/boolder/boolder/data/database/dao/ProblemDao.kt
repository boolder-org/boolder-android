package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.entity.ProblemWithAreaName

@Dao
interface ProblemDao {

    @Query("SELECT * FROM problems WHERE id == :problemId")
    suspend fun loadById(problemId: Int): ProblemEntity?

    @Query("SELECT * FROM problems WHERE id IN (:problemIds)")
    suspend fun loadAllByIds(problemIds: List<Int>): List<ProblemEntity>

    @Query(
        """
        SELECT problems.*, areas.name AS 'areaName' FROM problems, areas 
        WHERE problems.name_searchable LIKE :name AND problems.area_id = areas.id
        ORDER BY problems.popularity DESC 
        LIMIT 20
        """
    )
    suspend fun problemsByName(name: String): List<ProblemWithAreaName>

    @Query("SELECT * FROM problems WHERE problems.id IN (:problemIds) AND problems.name_searchable LIKE :name ORDER BY problems.popularity DESC")
    suspend fun getProblemsByIdsAndName(problemIds: List<Int>, name: String): List<ProblemEntity>
}