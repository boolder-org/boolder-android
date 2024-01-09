package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Area

fun dummyArea() = Area(
    id = 1,
    name = "Roche aux Oiseaux",
    description = "La Roche aux Oiseaux is a small and quite nice area.",
    warning = "February 2022: the orange circuit's paint is not visible anymore.",
    tags = listOf(
        R.string.tags_popular,
        R.string.tags_beginner_friendly,
        R.string.tags_family_friendly
    ),
    southWestLat = 0f,
    southWestLon = 0f,
    northEastLat = 0f,
    northEastLon = 0f
)
