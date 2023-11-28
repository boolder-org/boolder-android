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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.offline.dummyOfflineAreaDownloader
import com.boolder.boolder.utils.previewgenerator.dummyOfflineAreaItem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.MapViewModel
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem

@Composable
fun MapHeaderLayout(
    offlineAreaItem: OfflineAreaItem?,
    circuitState: MapViewModel.CircuitState?,
    gradeState: MapViewModel.GradeState,
    popularState: MapViewModel.PopularFilterState,
    shouldShowFiltersBar: Boolean,
    offlineAreaDownloader: OfflineAreaDownloader,
    onHideAreaName: () -> Unit,
    onSearchBarClicked: () -> Unit,
    onCircuitFilterChipClicked: () -> Unit,
    onGradeFilterChipClicked: () -> Unit,
    onPopularFilterChipClicked: () -> Unit,
    onResetFiltersClicked: () -> Unit,
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
            offlineAreaDownloader = offlineAreaDownloader,
            onHideAreaName = onHideAreaName,
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
                showCircuitFilterChip = offlineAreaItem != null,
                onCircuitFilterChipClicked = onCircuitFilterChipClicked,
                onGradeFilterChipClicked = onGradeFilterChipClicked,
                onPopularFilterChipClicked = onPopularFilterChipClicked,
                onResetFiltersClicked = onResetFiltersClicked
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
    showCircuitFilterChip: Boolean,
    onCircuitFilterChipClicked: () -> Unit,
    onGradeFilterChipClicked: () -> Unit,
    onPopularFilterChipClicked: () -> Unit,
    onResetFiltersClicked: () -> Unit
) {
    val isCircuitFilterActive = circuitState != null
    val isGradeFilterActive = gradeState.grades != ALL_GRADES
    val isPopularFilterActive = popularState.isEnabled

    val showResetButton = isCircuitFilterActive || isGradeFilterActive || isPopularFilterActive

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (showResetButton) {
            item(key = "reset_button") {
                MapFilterResetChip(
                    modifier = Modifier.animateItemPlacement(),
                    onClick = onResetFiltersClicked
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
                    onClick = onCircuitFilterChipClicked
                )
            }
        }

        item(key = gradeState.gradeRangeButtonTitle) {
            MapFilterChip(
                modifier = Modifier.animateItemPlacement(),
                selected = isGradeFilterActive,
                label = gradeState.gradeRangeButtonTitle,
                iconRes = R.drawable.ic_signal_cellular_alt,
                onClick = onGradeFilterChipClicked
            )
        }

        item(key = "popular-filter") {
            MapFilterChip(
                selected = isPopularFilterActive,
                label = stringResource(id = R.string.popular),
                iconRes = R.drawable.ic_favorite_border,
                onClick = onPopularFilterChipClicked
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        colors = ButtonDefaults.elevatedButtonColors(contentColor = Color.Black),
        onClick = onClick,
        content = {
            Icon(
                painter = painterResource(id = R.drawable.ic_cancel),
                contentDescription = null
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
            labelColor = Color.Black,
            iconColor = Color.Black
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

@Preview
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
            shouldShowFiltersBar = true,
            offlineAreaDownloader = dummyOfflineAreaDownloader(),
            onHideAreaName = {},
            onSearchBarClicked = {},
            onCircuitFilterChipClicked = {},
            onGradeFilterChipClicked = {},
            onPopularFilterChipClicked = {},
            onResetFiltersClicked = {}
        )
    }
}
