package com.boolder.boolder.view.map.composable

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
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
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.MapViewModel

@Composable
fun MapHeaderLayout(
    areaName: String?,
    circuitState: MapViewModel.CircuitState?,
    gradeState: MapViewModel.GradeState,
    shouldShowFiltersBar: Boolean,
    onHideAreaName: () -> Unit,
    onSearchBarClicked: () -> Unit,
    onCircuitFilterChipClicked: () -> Unit,
    onGradeFilterChipClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp)
    ) {
        MapTopBar(
            areaName = areaName,
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
                showCircuitFilterChip = areaName != null,
                onCircuitFilterChipClicked = onCircuitFilterChipClicked,
                onGradeFilterChipClicked = onGradeFilterChipClicked
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FiltersRow(
    circuitState: MapViewModel.CircuitState?,
    gradeState: MapViewModel.GradeState,
    showCircuitFilterChip: Boolean,
    onCircuitFilterChipClicked: () -> Unit,
    onGradeFilterChipClicked: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(8.dp)
    ) {
        if (showCircuitFilterChip) {
            item(key = circuitState?.circuitId) {
                MapFilterChip(
                    modifier = Modifier.animateItemPlacement(),
                    selected = circuitState != null,
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
                selected = gradeState.grades != ALL_GRADES,
                label = gradeState.gradeRangeButtonTitle,
                iconRes = R.drawable.ic_signal_cellular_alt,
                onClick = onGradeFilterChipClicked
            )
        }
    }
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
            areaName = "Apremont",
            circuitState = null,
            gradeState = MapViewModel.GradeState(
                gradeRangeButtonTitle = stringResource(id = R.string.grade),
                grades = ALL_GRADES
            ),
            shouldShowFiltersBar = true,
            onHideAreaName = {},
            onSearchBarClicked = {},
            onCircuitFilterChipClicked = {},
            onGradeFilterChipClicked = {}
        )
    }
}
