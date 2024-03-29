package com.boolder.boolder.view.ticklist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExportableTickList(
    @SerialName("ticks") val tickedProblemIds: List<ExportableTick>,
    @SerialName("projects") val projectIds: List<ExportableTick>
)

@Serializable
data class ExportableTick(
    @SerialName("id") val id: Int
)
