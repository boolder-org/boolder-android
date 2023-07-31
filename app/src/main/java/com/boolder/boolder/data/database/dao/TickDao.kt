package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boolder.boolder.data.database.entity.TickEntity


@Dao
interface TickDao {
    @Query("SELECT * FROM ticks")
    suspend fun getAllIds(): List<TickEntity>

    @Query("DELETE FROM ticks")
    suspend fun deleteAll()

    @Query("SELECT * FROM ticks WHERE id == :tickId")
    suspend fun loadById(tickId: Int): TickEntity?
    @Insert
    suspend fun insertTick(tick: TickEntity)
}