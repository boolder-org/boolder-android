package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.AreasEntity

@Dao
interface AreaDao {

    @Query("SELECT * FROM Areas")
    suspend fun getAll(): List<AreasEntity>

    @Query("SELECT * FROM Areas WHERE id IN (:areaIds)")
    suspend fun loadAllByIds(areaIds: List<Int>): List<AreasEntity>
}