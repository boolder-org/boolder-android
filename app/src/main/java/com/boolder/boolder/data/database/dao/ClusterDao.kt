package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.ClusterEntity

@Dao
interface ClusterDao {

    @Query("SELECT * FROM clusters WHERE id = :clusterId")
    suspend fun getClusterById(clusterId: Int): ClusterEntity?
}
