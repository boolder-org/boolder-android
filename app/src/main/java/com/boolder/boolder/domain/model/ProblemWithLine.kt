package com.boolder.boolder.domain.model

/**
 * Represents a boulder problem and its associated line. The line is nullable
 * in case of a failure when parsing the line coordinates.
 */
data class ProblemWithLine(
    val problem: Problem,
    val line: Line?
)
