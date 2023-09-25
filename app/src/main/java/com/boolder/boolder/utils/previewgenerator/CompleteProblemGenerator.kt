package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.Problem

fun dummyCompleteProblem() = CompleteProblem(
    problem = Problem(
        id = 1000,
        name = null,
        nameEn = null,
        grade = null,
        latitude = 0f,
        longitude = 0f,
        circuitId = null,
        circuitNumber = "10",
        circuitColor = "RED",
        steepness = "",
        sitStart = false,
        areaId = 1000,
        bleauInfoId = null,
        featured = false,
        parentId = null,
        areaName = null
    ),
    topo = null,
    line = dummyLine(),
    otherCompleteProblem = emptyList()
)
