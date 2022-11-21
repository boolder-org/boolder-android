package com.boolder.boolder.data.network.repository

import com.boolder.boolder.data.network.KtorClient
import com.boolder.boolder.data.network.model.TopoRemote

class TopoRepository(
    private val client: KtorClient
) {

    suspend fun getTopoById(topoId: Int): Result<TopoRemote> {
        return client.loadTopoPicture(topoId)
    }
}