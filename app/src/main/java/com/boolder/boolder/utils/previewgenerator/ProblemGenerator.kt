package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.domain.model.Problem

fun dummyProblem(
    id: Int = 1000,
    name: String = "The dummy problem",
    circuitNumber: String? = "10",
    circuitColor: String? = "RED",
    featured: Boolean = false
) = Problem(
    id = id,
    name = name,
    nameEn = name,
    grade = "5b",
    latitude = 0f,
    longitude = 0f,
    circuitId = null,
    circuitNumber = circuitNumber,
    circuitColor = circuitColor,
    steepness = "wall",
    sitStart = true,
    areaId = 1000,
    bleauInfoId = null,
    featured = featured,
    parentId = null,
    areaName = null
)
