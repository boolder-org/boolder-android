package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.AreasEntity

@Dao
interface AreaDao {

    @Query("SELECT * FROM areas WHERE name_searchable LIKE :name ORDER BY priority ASC LIMIT 10")
    suspend fun areasByName(name: String): List<AreasEntity>

    @Query("SELECT * FROM areas WHERE id = :id")
    suspend fun getAreaById(id: Int): AreasEntity

    @Query("SELECT * FROM areas ORDER BY name_searchable ASC")
    suspend fun getAllAreas(): List<AreasEntity>
}
