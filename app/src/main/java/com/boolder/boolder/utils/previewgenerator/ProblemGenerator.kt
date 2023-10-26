package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.domain.model.Problem

fun dummyProblem(
    id: Int = 1000,
    name: String = "The dummy problem"
) = Problem(
    id = id,
    name = name,
    nameEn = name,
    grade = "5b",
    latitude = 0f,
    longitude = 0f,
    circuitId = null,
    circuitNumber = "10",
    circuitColor = "RED",
    steepness = "wall",
    sitStart = true,
    areaId = 1000,
    bleauInfoId = null,
    featured = false,
    parentId = null,
    areaName = null
)
