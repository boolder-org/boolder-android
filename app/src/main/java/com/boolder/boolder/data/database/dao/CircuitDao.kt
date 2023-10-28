package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.CircuitEntity

@Dao
interface CircuitDao {

    @Query(
        """
            SELECT * FROM circuits 
            WHERE id IN (
                SELECT circuit_id FROM problems
                WHERE area_id = :areaId AND circuit_id IS NOT NULL
                GROUP BY circuit_id
                HAVING COUNT(*) >= 10
            )
            ORDER BY average_grade ASC
        """
    )
    suspend fun getAvailableCircuits(areaId: Int): List<CircuitEntity>

    @Query("""
        SELECT * FROM circuits
        WHERE id = (SELECT circuit_id FROM problems WHERE id = :problemId)
        """
    )
    suspend fun getCircuitFromProblemId(problemId: Int): CircuitEntity?
}
