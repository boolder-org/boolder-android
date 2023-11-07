package com.boolder.boolder.data.network.repository

import com.boolder.boolder.data.network.KtorClient
import com.boolder.boolder.data.network.model.TopoUrl

class TopoRepository(
    private val client: KtorClient
) {

    suspend fun getTopoPictureById(topoId: Int): String? =
        client.loadTopoPicture(topoId).getOrNull()?.url

    suspend fun getTopoPicturesForArea(areaId: Int): List<TopoUrl> =
        client.loadTopoPicturesForArea(areaId).getOrElse { emptyList() }
}
