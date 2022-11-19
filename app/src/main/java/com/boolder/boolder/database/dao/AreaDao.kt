package com.boolder.boolder.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.database.entity.Areas

@Dao
interface AreaDao {

    @Query("SELECT * FROM Areas")
    suspend fun getAll(): List<Areas>

    @Query("SELECT * FROM Areas WHERE id IN (:areaIds)")
    suspend fun loadAllByIds(areaIds: List<Int>): List<Areas>
}