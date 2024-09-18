package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.ClusterDao
import com.boolder.boolder.domain.convert
import com.boolder.boolder.domain.model.Cluster

class ClusterRepository(
    private val clusterDao: ClusterDao
) {

    suspend fun getClusterById(clusterId: Int): Cluster? =
        clusterDao.getClusterById(clusterId)?.convert()
}
