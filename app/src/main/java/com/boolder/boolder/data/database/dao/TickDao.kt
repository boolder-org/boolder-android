package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boolder.boolder.data.database.entity.Tick

@Dao
interface TickDao {
    @Query("SELECT * FROM tick")
    fun getAllIds(): List<Tick>

    @Query("SELECT * FROM tick WHERE id == :tickId")
    suspend fun loadById(tickId: Int): Tick?
    @Insert
    suspend fun insertTick(tick: Tick)
}