package com.boolder.boolder.view.map.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.utils.extension.composeColor
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.MapViewModel
import com.boolder.boolder.view.map.filter.DummyFiltersEventHandler
import com.boolder.boolder.view.map.filter.FiltersEventHandler
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus

@Composable
fun MapControlsOverlay(
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
    onCircuitStartClicked: () -> Unit,
    onDownloadAreaClicked: () -> Unit,
    onFindMyPositionClicked: () -> Unit,
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
            projectsState = projectsState,
            tickedState = tickedState,
            shouldShowFiltersBar = shouldShowFiltersBar,
            filtersEventHandler = filtersEventHandler,
            onHideAreaName = onHideAreaName,
            onAreaInfoClicked = onAreaInfoClicked,
            onSearchBarClicked = onSearchBarClicked
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            MapFloatingActionButtons(
                modifier = Modifier.align(Alignment.BottomEnd),
                offlineAreaItem = offlineAreaItem,
                onDownloadAreaClicked = onDownloadAreaClicked,
                onFindMyPositionClicked = onFindMyPositionClicked
            )

            if (circuitState?.showCircuitStartButton == true) {
                Button(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.elevatedButtonColors(),
                    onClick = onCircuitStartClicked
                ) {
                    Text(
                        text = stringResource(id = R.string.circuit_start),
                        style = MaterialTheme.typography.labelLarge,
                        color = when (val circuitColor = circuitState.color) {
                            CircuitColor.WHITE,
                            CircuitColor.BLACK -> MaterialTheme.colorScheme.onSurface

                            else -> circuitColor.composeColor()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MapFloatingActionButtons(
    offlineAreaItem: OfflineAreaItem?,
    onDownloadAreaClicked: () -> Unit,
    onFindMyPositionClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = offlineAreaItem != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SmallFloatingActionButton(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                onClick = onDownloadAreaClicked
            ) {
                when (offlineAreaItem?.status) {
                    is OfflineAreaItemStatus.Downloading -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )

                    is OfflineAreaItemStatus.Downloaded -> Icon(
                        painter = painterResource(id = R.drawable.ic_download_done),
                        contentDescription = stringResource(id = R.string.cd_downloaded_area),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    else -> Icon(
                        painter = painterResource(id = R.drawable.ic_download_for_offline),
                        contentDescription = stringResource(id = R.string.cd_download_area)
                    )
                }
            }
        }

        SmallFloatingActionButton(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
            onClick = onFindMyPositionClicked
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_fab_near_me),
                contentDescription = stringResource(id = R.string.cd_find_my_position)
            )
        }
    }
}

@PreviewLightDark
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
            projectsState = MapViewModel.ProjectsFilterState(projectIds = emptyList()),
            tickedState = MapViewModel.TickedFilterState(tickedProblemIds = emptyList()),
            shouldShowFiltersBar = true,
            filtersEventHandler = DummyFiltersEventHandler,
            onHideAreaName = {},
            onAreaInfoClicked = {},
            onSearchBarClicked = {},
            onCircuitStartClicked = {},
            onDownloadAreaClicked = {},
            onFindMyPositionClicked = {}
        )
    }
}
