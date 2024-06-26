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

    @Query("SELECT * FROM problems WHERE id = :id")
    suspend fun problemById(id: Int): ProblemEntity?

    @Query("SELECT * FROM problems WHERE parent_id = :parentProblemId")
    suspend fun problemVariantsByParentId(parentProblemId: Int): List<ProblemEntity>

    @Query(
        """
        SELECT id FROM problems 
        WHERE circuit_id = :circuitId AND circuit_number = :circuitProblemNumber
        """
    )
    suspend fun problemIdByCircuitAndNumber(
        circuitId: Int,
        circuitProblemNumber: String
    ): Int?

    @Query("""
        SELECT * FROM problems
        WHERE area_id = :areaId
        ORDER BY grade DESC, popularity DESC
        """
    )
    suspend fun problemsForArea(areaId: Int): List<ProblemEntity>

    @Query("""
        SELECT * FROM problems
        WHERE area_id = :areaId and name_searchable LIKE '%' || :query || '%'
        ORDER BY grade DESC, popularity DESC
        """
    )
    suspend fun problemsForArea(areaId: Int, query: String): List<ProblemEntity>

    @Query("""
        SELECT * FROM problems 
        WHERE circuit_id = :circuitId
        ORDER BY CAST(circuit_number AS INTEGER)
        """
    )
    suspend fun problemsForCircuit(circuitId: Int): List<ProblemEntity>
}
