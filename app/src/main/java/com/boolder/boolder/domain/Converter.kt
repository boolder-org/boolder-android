package com.boolder.boolder.domain

import com.boolder.boolder.R
import com.boolder.boolder.data.database.entity.AreasEntity
import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.entity.ProblemWithAreaName
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import java.util.Locale

fun ProblemEntity.convert(areaName: String? = null): Problem {
    return Problem(
        id,
        name,
        nameEn,
        grade,
        latitude,
        longitude,
        circuitId,
        circuitNumber,
        circuitColor,
        steepness,
        sitStart,
        areaId,
        bleauInfoId,
        featured,
        parentId,
        areaName
    )
}

fun ProblemWithAreaName.convert() = Problem(
    problemEntity.id,
    problemEntity.name,
    problemEntity.nameEn,
    problemEntity.grade,
    problemEntity.latitude,
    problemEntity.longitude,
    problemEntity.circuitId,
    problemEntity.circuitNumber,
    problemEntity.circuitColor,
    problemEntity.steepness,
    problemEntity.sitStart,
    problemEntity.areaId,
    problemEntity.bleauInfoId,
    problemEntity.featured,
    problemEntity.parentId,
    areaName
)

fun LineEntity.convert(): Line {
    return Line(id, problemId, topoId, coordinates)
}

fun AreasEntity.convert(): Area {
    val language = Locale.getDefault().language
    val description = if (language == "fr") descriptionFr else descriptionEn
    val warning = if (language == "fr") warningFr else warningEn
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
