package com.boolder.boolder.data.network.repository

import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.network.model.TopoUrl

class TopoRepository(
    private val areaRepository: AreaRepository
) {

    fun getTopoPictureById(topoId: Int): String =
        TOPO_URL_BASE + topoId

    suspend fun getTopoPicturesForArea(areaId: Int): List<TopoUrl> =
        areaRepository.getAllTopoIdsForArea(areaId).map {
            TopoUrl(
                id = it,
                url = TOPO_URL_BASE + it
            )
        }

    companion object {
        private const val TOPO_URL_BASE = "https://assets.boolder.com/proxy/topos/"
    }
}
