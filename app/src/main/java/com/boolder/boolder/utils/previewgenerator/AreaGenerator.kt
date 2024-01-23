package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Area

fun dummyArea(
    id: Int = 1,
    name: String = "Roche aux Oiseaux",
    description: String = "La Roche aux Oiseaux is a small and quite nice area.",
    problemsCount: Int = 513
) = Area(
    id = id,
    name = name,
    description = description,
    warning = "February 2022: the orange circuit's paint is not visible anymore.",
    tags = listOf(
        R.string.tags_popular,
        R.string.tags_beginner_friendly,
        R.string.tags_family_friendly
    ),
    southWestLat = 0f,
    southWestLon = 0f,
    northEastLat = 0f,
    northEastLon = 0f,
    problemsCount = problemsCount,
    problemsCountsPerGrade = mapOf(
        "1" to 3,
        "2" to 103,
        "3" to 63,
        "4" to 88,
        "5" to 60,
        "6" to 84,
        "7" to 97,
        "8" to 15,
    )
)
