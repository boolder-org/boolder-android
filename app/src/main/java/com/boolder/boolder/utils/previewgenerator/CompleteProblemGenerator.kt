package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.domain.model.CompleteProblem

fun dummyCompleteProblem() = CompleteProblem(
    problemWithLine = dummyProblemWithLine(),
    variants = emptyList()
)
