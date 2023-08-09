package com.boolder.boolder.data.database.entity

data class AreaWithProblems(
    val areaEntity: AreasEntity,
    val problems: List<ProblemEntity>
)
