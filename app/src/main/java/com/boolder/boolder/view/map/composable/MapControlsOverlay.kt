package com.boolder.boolder.view.map.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.utils.extension.composeColor
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.MapViewModel

@Composable
fun MapControlsOverlay(
    areaName: String?,
    circuitState: MapViewModel.CircuitState?,
    gradeState: MapViewModel.GradeState,
    shouldShowFiltersBar: Boolean,
    onHideAreaName: () -> Unit,
    onSearchBarClicked: () -> Unit,
    onCircuitFilterChipClicked: () -> Unit,
    onGradeFilterChipClicked: () -> Unit,
    onCircuitStartClicked: () -> Unit,
    onResetFiltersClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        MapHeaderLayout(
            areaName = areaName,
            circuitState = circuitState,
            gradeState = gradeState,
            shouldShowFiltersBar = shouldShowFiltersBar,
            onHideAreaName = onHideAreaName,
            onSearchBarClicked = onSearchBarClicked,
            onCircuitFilterChipClicked = onCircuitFilterChipClicked,
            onGradeFilterChipClicked = onGradeFilterChipClicked,
            onResetFiltersClicked = onResetFiltersClicked
        )

        Spacer(modifier = Modifier.weight(1f))

        circuitState?.let {
            var showButton by remember(circuitState) { mutableStateOf(true) }

            if (!showButton) return@let

            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.elevatedButtonColors(),
                onClick = {
                    onCircuitStartClicked()
                    showButton = false
                }
            ) {
                Text(
                    text = stringResource(id = R.string.circuit_start),
                    style = MaterialTheme.typography.labelLarge,
                    color = when (val circuitColor = circuitState.color) {
                        CircuitColor.WHITE -> Color.Black
                        else -> circuitColor.composeColor()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun MapControlsOverlayPreview() {
    BoolderTheme {
        MapControlsOverlay(
            areaName = "Apremont",
            circuitState = MapViewModel.CircuitState(
                circuitId = 0,
                color = CircuitColor.ORANGE
            ),
            gradeState = MapViewModel.GradeState(
                gradeRangeButtonTitle = stringResource(id = R.string.grade),
                grades = ALL_GRADES
            ),
            shouldShowFiltersBar = true,
            onHideAreaName = {},
            onSearchBarClicked = {},
            onCircuitFilterChipClicked = {},
            onGradeFilterChipClicked = {},
            onCircuitStartClicked = {},
            onResetFiltersClicked = {}
        )
    }
}
