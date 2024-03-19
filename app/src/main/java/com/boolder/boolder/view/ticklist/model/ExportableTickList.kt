package com.boolder.boolder.view.ticklist.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExportableTickList(
    @SerialName("ticks") val tickedProblemIds: List<Int>,
    @SerialName("projects") val projectIds: List<Int>
)
