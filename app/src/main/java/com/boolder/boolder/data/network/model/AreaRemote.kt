package com.boolder.boolder.data.network.model

import com.boolder.boolder.view.search.BaseObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AreaRemote(
    val objectID: String,
    val name: String,
    val bounds: BoundsRemote
) : BaseObject {
    @Serializable
    data class BoundsRemote(
        @SerialName("south_west")
        val southWest: AlgoliaPoint,
        @SerialName("north_east")
        val northEast: AlgoliaPoint
    ) {
        @Serializable
        data class AlgoliaPoint(val lat: Double, val lng: Double)
    }
}
