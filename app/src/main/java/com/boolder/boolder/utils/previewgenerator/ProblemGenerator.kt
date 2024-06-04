package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.data.userdatabase.entity.TickStatus
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Steepness

fun dummyProblem(
    id: Int = 1000,
    name: String = "The dummy problem",
    circuitNumber: String? = "10",
    circuitColor: String? = "RED",
    steepness: Steepness? = Steepness.WALL,
    sitStart: Boolean = true,
    featured: Boolean = false,
    areaName: String? = null,
    tickStatus: TickStatus? = null
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
    steepness = steepness,
    sitStart = sitStart,
    areaId = 1000,
    bleauInfoId = null,
    featured = featured,
    parentId = null,
    areaName = areaName,
    tickStatus = tickStatus
)
