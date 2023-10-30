package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.domain.model.ProblemWithLine

fun dummyProblemWithLine(
    id: Int = 1000,
    name: String = "The dummy problem"
) = ProblemWithLine(
    problem = dummyProblem(id = id, name = name),
    line = dummyLine()
)
