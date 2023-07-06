package com.boolder.boolder.view.search.model

import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Problem

data class SearchResult(
    val areas: List<Area>,
    val problems: List<Problem>
) {
    fun isEmpty() = areas.isEmpty() && problems.isEmpty()

    companion object {
        val EMPTY = SearchResult(
            areas = emptyList(),
            problems = emptyList()
        )
    }
}
