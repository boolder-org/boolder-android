package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.TopoEntity

@Dao
interface TopoDao {

    @Query("SELECT * FROM topos WHERE id = :topoId")
    suspend fun loadById(topoId: Int): TopoEntity?

    @Query("""
        SELECT * FROM topos
        WHERE boulder_id = :boulderId
            AND id IN (SELECT topo_id FROM lines)
        ORDER BY position ASC
    """)
    suspend fun loadAllByBoulderId(boulderId: Int): List<TopoEntity>
}
