package com.boolder.boolder.data.userdatabase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boolder.boolder.data.userdatabase.entity.TickedProblemEntity

@Dao
interface TickedProblemDao {

    @Query("SELECT * FROM ticked_problems")
    suspend fun getAllTickedProblems(): List<TickedProblemEntity>

    @Query("SELECT * FROM ticked_problems WHERE problem_id = :problemId")
    suspend fun getTickedProblemByProblemId(problemId: Int): TickedProblemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickedProblem(tickedProblem: TickedProblemEntity)

    @Query("DELETE FROM ticked_problems WHERE problem_id = :problemId")
    suspend fun deleteTickedProblemByProblemId(problemId: Int)
}
