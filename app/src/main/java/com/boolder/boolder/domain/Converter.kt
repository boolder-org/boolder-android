package com.boolder.boolder.domain

import com.boolder.boolder.R
import com.boolder.boolder.data.database.entity.AreasEntity
import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.entity.ProblemWithAreaName
import com.boolder.boolder.data.userdatabase.entity.TickStatus
import com.boolder.boolder.data.userdatabase.entity.TickedProblemEntity
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.TickedProblem
import com.boolder.boolder.utils.getLanguage

fun ProblemEntity.convert(
    areaName: String? = null,
    tickStatus: TickStatus? = null
): Problem {
    return Problem(
        id = id,
        name = name,
        nameEn = nameEn,
        grade = grade,
        latitude = latitude,
        longitude = longitude,
        circuitId = circuitId,
        circuitNumber = circuitNumber,
        circuitColor = circuitColor,
        steepness = steepness,
        sitStart = sitStart,
        areaId = areaId,
        bleauInfoId = bleauInfoId,
        featured = featured,
        parentId = parentId,
        areaName = areaName,
        tickStatus = tickStatus
    )
}

fun ProblemWithAreaName.convert() = Problem(
    id = problemEntity.id,
    name = problemEntity.name,
    nameEn = problemEntity.nameEn,
    grade = problemEntity.grade,
    latitude = problemEntity.latitude,
    longitude = problemEntity.longitude,
    circuitId = problemEntity.circuitId,
    circuitNumber = problemEntity.circuitNumber,
    circuitColor = problemEntity.circuitColor,
    steepness = problemEntity.steepness,
    sitStart = problemEntity.sitStart,
    areaId = problemEntity.areaId,
    bleauInfoId = problemEntity.bleauInfoId,
    featured = problemEntity.featured,
    parentId = problemEntity.parentId,
    areaName = areaName,
    tickStatus = null
)

fun LineEntity.convert(): Line {
    return Line(id, problemId, topoId, coordinates)
}

fun AreasEntity.convert(): Area {
    val (description, warning) = when (getLanguage()) {
        "fr" -> descriptionFr to warningFr
        else -> descriptionEn to warningEn
    }

    val rawTags = tags?.split(",") ?: emptyList()
    val tags = rawTags.mapNotNull {
        when (it) {
            "popular" -> R.string.tags_popular
            "beginner_friendly" -> R.string.tags_beginner_friendly
            "family_friendly" -> R.string.tags_family_friendly
            "dry_fast" -> R.string.tags_dry_fast
            else -> null
        }
    }

    return Area(
        id = id,
        name = name,
        description = description,
        warning = warning,
        tags = tags,
        southWestLat = southWestLat,
        southWestLon = southWestLon,
        northEastLat = northEastLat,
        northEastLon = northEastLon,
        problemsCount = problemsCount,
        problemsCountsPerGrade = buildMap {
            put("1", level1Count)
            put("2", level2Count)
            put("3", level3Count)
            put("4", level4Count)
            put("5", level5Count)
            put("6", level6Count)
            put("7", level7Count)
            put("8", level8Count)
        }
    )
}

fun TickedProblemEntity.convert() = TickedProblem(
    problemId = problemId,
    tickStatus = tickStatus
)
