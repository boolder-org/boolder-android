package com.boolder.boolder.domain

import com.boolder.boolder.data.database.entity.AreasEntity
import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.ProblemEntity
import com.boolder.boolder.data.network.model.TopoRemote
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Topo

fun ProblemEntity.convert(areaName: String? = null): Problem {
    return Problem(
        id,
        name,
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

fun TopoRemote.convert(): Topo {
    return Topo(url)
}

fun LineEntity.convert(): Line {
    return Line(id, problemId, topoId, coordinates)
}

fun AreasEntity.convert(): Area {
    return Area(id, name, southWestLat, southWestLon, northEastLat, northEastLon)
}