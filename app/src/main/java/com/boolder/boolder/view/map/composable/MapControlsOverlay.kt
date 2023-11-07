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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.offline.dummyOfflineAreaDownloader
import com.boolder.boolder.utils.extension.composeColor
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.MapViewModel
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus

@Composable
fun MapControlsOverlay(
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
    onCircuitStartClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.systemBarsPadding()
    ) {
        MapHeaderLayout(
            offlineAreaItem = offlineAreaItem,
            circuitState = circuitState,
            gradeState = gradeState,
            popularState = popularState,
            shouldShowFiltersBar = shouldShowFiltersBar,
            offlineAreaDownloader = offlineAreaDownloader,
            onHideAreaName = onHideAreaName,
            onSearchBarClicked = onSearchBarClicked,
            onCircuitFilterChipClicked = onCircuitFilterChipClicked,
            onGradeFilterChipClicked = onGradeFilterChipClicked,
            onPopularFilterChipClicked = onPopularFilterChipClicked,
            onResetFiltersClicked = onResetFiltersClicked,
        )

        Spacer(modifier = Modifier.weight(1f))

        if (circuitState?.showCircuitStartButton == true) {
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.elevatedButtonColors(),
                onClick = onCircuitStartClicked
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
            offlineAreaItem = OfflineAreaItem(
                area = dummyArea(),
                status = OfflineAreaItemStatus.NotDownloaded
            ),
            circuitState = MapViewModel.CircuitState(
                circuitId = 0,
                color = CircuitColor.ORANGE,
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
            onResetFiltersClicked = {},
            onCircuitStartClicked = {}
        )
    }
}
