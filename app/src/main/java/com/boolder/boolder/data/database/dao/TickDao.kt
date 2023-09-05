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

    @Query("SELECT * FROM ticks WHERE id == :tickId AND state == :state")
    suspend fun loadByIdAndState(tickId: Int, state: Int): TickEntity?

    @Insert
    suspend fun insertTick(tick: TickEntity)

    @Query("DELETE FROM ticks WHERE id = :tickId")
    suspend fun deleteById(tickId: Int)

    @Query("UPDATE ticks SET state = :state WHERE id = :tickId")
    suspend fun update(tickId: Int, state: Int)
}