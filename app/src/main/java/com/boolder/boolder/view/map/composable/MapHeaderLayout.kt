package com.boolder.boolder.view.map.composable

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.utils.previewgenerator.dummyOfflineAreaItem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.MapViewModel
import com.boolder.boolder.view.map.filter.DummyFiltersEventHandler
import com.boolder.boolder.view.map.filter.FiltersEventHandler
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem

@Composable
fun MapHeaderLayout(
    offlineAreaItem: OfflineAreaItem?,
    circuitState: MapViewModel.CircuitState?,
    gradeState: MapViewModel.GradeState,
    popularState: MapViewModel.PopularFilterState,
    projectsState: MapViewModel.ProjectsFilterState,
    tickedState: MapViewModel.TickedFilterState,
    shouldShowFiltersBar: Boolean,
    filtersEventHandler: FiltersEventHandler,
    onHideAreaName: () -> Unit,
    onAreaInfoClicked: () -> Unit,
    onSearchBarClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp)
    ) {
        MapTopBar(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            offlineAreaItem = offlineAreaItem,
            onHideAreaName = onHideAreaName,
            onAreaInfoClicked = onAreaInfoClicked,
            onSearchBarClicked = onSearchBarClicked
        )

        AnimatedVisibility(
            visible = shouldShowFiltersBar,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FiltersRow(
                circuitState = circuitState,
                gradeState = gradeState,
                popularState = popularState,
                projectsState = projectsState,
                tickedState = tickedState,
                showCircuitFilterChip = offlineAreaItem != null,
                filtersEventHandler = filtersEventHandler
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FiltersRow(
    circuitState: MapViewModel.CircuitState?,
    gradeState: MapViewModel.GradeState,
    popularState: MapViewModel.PopularFilterState,
    projectsState: MapViewModel.ProjectsFilterState,
    tickedState: MapViewModel.TickedFilterState,
    showCircuitFilterChip: Boolean,
    filtersEventHandler: FiltersEventHandler
) {
    val lazyRowState = rememberLazyListState()

    val isCircuitFilterActive = circuitState != null
    val isGradeFilterActive = gradeState.grades != ALL_GRADES
    val isPopularFilterActive = popularState.isEnabled
    val isProjectsFilterActive = projectsState.projectIds.isNotEmpty()
    val isTickedFilterActive = tickedState.tickedProblemIds.isNotEmpty()

    val showResetButton = isCircuitFilterActive || isGradeFilterActive || isPopularFilterActive

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        state = lazyRowState,
        horizontalArrangement = spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (showResetButton) {
            item(key = "reset_button") {
                MapFilterResetChip(
                    modifier = Modifier.animateItemPlacement(),
                    onClick = filtersEventHandler::onResetFiltersButtonClicked
                )
            }
        }

        if (showCircuitFilterChip) {
            item(key = circuitState?.circuitId) {
                MapFilterChip(
                    modifier = Modifier.animateItemPlacement(),
                    selected = isCircuitFilterActive,
                    label = circuitState?.color?.localizedName()
                        ?: stringResource(id = R.string.circuits),
                    iconRes = R.drawable.ic_route,
                    onClick = filtersEventHandler::onCircuitFilterChipClicked
                )

                LaunchedEffect(key1 = circuitState) { lazyRowState.animateScrollToItem(index = 0) }
            }
        }

        item(key = gradeState.gradeRangeButtonTitle) {
            MapFilterChip(
                modifier = Modifier.animateItemPlacement(),
                selected = isGradeFilterActive,
                label = gradeState.gradeRangeButtonTitle,
                iconRes = R.drawable.ic_signal_cellular_alt,
                onClick = filtersEventHandler::onGradeFilterChipClicked
            )
        }

        item(key = "popular-filter") {
            MapFilterChip(
                modifier = Modifier.animateItemPlacement(),
                selected = isPopularFilterActive,
                label = stringResource(id = R.string.filter_popular),
                iconRes = R.drawable.ic_favorite_border,
                onClick = filtersEventHandler::onPopularFilterChipClicked
            )
        }

        item(key = "projects-filter") {
            MapFilterChip(
                modifier = Modifier.animateItemPlacement(),
                selected = isProjectsFilterActive,
                label = stringResource(id = R.string.filter_projects),
                iconRes = R.drawable.ic_star_outline,
                onClick = filtersEventHandler::onProjectsFilterChipClicked
            )
        }

        item(key = "ticked-filter") {
            MapFilterChip(
                modifier = Modifier.animateItemPlacement(),
                selected = isTickedFilterActive,
                label = stringResource(id = R.string.filter_ticked),
                iconRes = R.drawable.ic_check_circle,
                onClick = filtersEventHandler::onTickedFilterChipClicked
            )
        }
    }

    LaunchedEffect(key1 = Unit) { lazyRowState.scrollToItem(index = 0) }
}

@Composable
private fun MapFilterResetChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        modifier = modifier
            .padding(vertical = 8.dp)
            .size(FilterChipDefaults.Height),
        shape = CircleShape,
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = onClick,
        content = {
            Icon(
                painter = painterResource(id = R.drawable.ic_cancel),
                contentDescription = null
            )
        }
    )
}

@Composable
private fun MapFilterChip(
    selected: Boolean,
    label: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedFilterChip(
        modifier = modifier,
        selected = selected,
        shape = CircleShape,
        colors = FilterChipDefaults.elevatedFilterChipColors(
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurface
        ),
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null
            )
        },
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
private fun MapHeaderLayoutPreview() {
    BoolderTheme {
        MapHeaderLayout(
            offlineAreaItem = dummyOfflineAreaItem(),
            circuitState = MapViewModel.CircuitState(
                circuitId = 42,
                color = CircuitColor.BLUE,
                showCircuitStartButton = true
            ),
            gradeState = MapViewModel.GradeState(
                gradeRangeButtonTitle = stringResource(id = R.string.grade),
                grades = ALL_GRADES
            ),
            popularState = MapViewModel.PopularFilterState(isEnabled = false),
            projectsState = MapViewModel.ProjectsFilterState(projectIds = emptyList()),
            tickedState = MapViewModel.TickedFilterState(tickedProblemIds = emptyList()),
            shouldShowFiltersBar = true,
            filtersEventHandler = DummyFiltersEventHandler,
            onHideAreaName = {},
            onAreaInfoClicked = {},
            onSearchBarClicked = {}
        )
    }
}
