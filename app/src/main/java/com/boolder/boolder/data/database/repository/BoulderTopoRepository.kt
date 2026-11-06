package com.boolder.boolder.data.database.repository

import com.boolder.boolder.data.database.dao.TopoDao
import com.boolder.boolder.data.database.entity.TopoEntity

class BoulderTopoRepository(
    private val topoDao: TopoDao
) {

    suspend fun loadById(topoId: Int): TopoEntity? {
        return topoDao.loadById(topoId)
    }

    suspend fun loadAllByBoulderId(boulderId: Int): List<TopoEntity> {
        return topoDao.loadAllByBoulderId(boulderId)
    }
}
