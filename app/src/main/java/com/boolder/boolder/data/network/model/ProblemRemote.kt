package com.boolder.boolder.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProblemRemote(
    val objectID: String,
    val name: String,
    val grade: String,
    @SerialName("area_name")
    val areaName: String
)
