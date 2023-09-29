package com.boolder.boolder.domain

import com.boolder.boolder.data.database.entity.AreasEntity
import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.database.entity.ProblemWithAreaName
import com.boolder.boolder.data.network.model.TopoRemote
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Topo

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
    return Area(id, name, southWestLat, southWestLon, northEastLat, northEastLon)
}
