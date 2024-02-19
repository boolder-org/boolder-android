package com.boolder.boolder.domain.model

import com.boolder.boolder.data.userdatabase.entity.TickStatus
import com.boolder.boolder.data.userdatabase.entity.TickedProblemEntity

data class TickedProblem(
    val problemId: Int,
    val tickStatus: TickStatus
) {
    fun toEntity() = TickedProblemEntity(
        id = 0,
        createdAt = System.currentTimeMillis(),
        problemId = problemId,
        tickStatus = tickStatus
    )
}
