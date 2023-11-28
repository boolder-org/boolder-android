package com.boolder.boolder.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopoUrl(
    @SerialName("topo_id") val id: Int,
    @SerialName("url") val url: String
)
