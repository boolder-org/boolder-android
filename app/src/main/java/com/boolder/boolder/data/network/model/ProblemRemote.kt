package com.boolder.boolder.data.network.model

import com.boolder.boolder.view.search.BaseObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProblemRemote(
    val objectID: String,
    val name: String,
    val grade: String,
    @SerialName("area_name")
    val areaName: String
) : BaseObject
