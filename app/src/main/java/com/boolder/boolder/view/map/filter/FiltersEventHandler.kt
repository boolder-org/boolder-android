package com.boolder.boolder.view.map.filter

interface FiltersEventHandler {
    fun onCircuitFilterChipClicked()
    fun onGradeFilterChipClicked()
    fun onPopularFilterChipClicked()
    fun onProjectsFilterChipClicked()
    fun onTickedFilterChipClicked()
    fun onResetFiltersButtonClicked()
}

object DummyFiltersEventHandler : FiltersEventHandler {
    override fun onCircuitFilterChipClicked() {}
    override fun onGradeFilterChipClicked() {}
    override fun onPopularFilterChipClicked() {}
    override fun onProjectsFilterChipClicked() {}
    override fun onTickedFilterChipClicked() {}
    override fun onResetFiltersButtonClicked() {}
}
