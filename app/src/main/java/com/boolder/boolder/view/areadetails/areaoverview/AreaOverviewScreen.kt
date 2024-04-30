package com.boolder.boolder.view.areadetails.areaoverview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.PoiTransport
import com.boolder.boolder.domain.model.PoiType
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.offline.dummyOfflineAreaDownloader
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.utils.previewgenerator.dummyCircuit
import com.boolder.boolder.view.areadetails.areaoverview.composable.acessfrompoi.AccessFromPoi
import com.boolder.boolder.view.areadetails.areaoverview.composable.download.AreaPhotosDownloadItem
import com.boolder.boolder.view.areadetails.composable.SectionContainer
import com.boolder.boolder.view.areadetails.composable.SeeOnMapButton
import com.boolder.boolder.view.compose.BoolderOrange
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.CircuitItem
import com.boolder.boolder.view.compose.degreecounts.DegreeCountsChart
import com.boolder.boolder.view.compose.degreecounts.DegreeCountsRow
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import com.boolder.boolder.domain.model.AccessFromPoi as AccessFromPoiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AreaOverviewScreen(
    screenState: AreaOverviewViewModel.ScreenState,
    offlineAreaDownloader: OfflineAreaDownloader,
    displayShowOnMapButton: Boolean,
    onBackPressed: () -> Unit,
    onSeeOnMapClicked: () -> Unit,
    onAreaProblemsCountClicked: () -> Unit,
    onCircuitClicked: (Int) -> Unit,
    onPoiClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (screenState !is AreaOverviewViewModel.ScreenState.Content) return@TopAppBar

                    Text(text = screenState.area.name)
                },
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(shape = CircleShape)
                            .clickable(onClick = onBackPressed)
                            .padding(8.dp),
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = null
                    )
                }
            )
        },
        floatingActionButton = {
            if (screenState is AreaOverviewViewModel.ScreenState.UnknownArea || !displayShowOnMapButton) {
                return@Scaffold
            }

            SeeOnMapButton(onClick = onSeeOnMapClicked)
        },
        floatingActionButtonPosition = FabPosition.Center,
        content = {
            when (screenState) {
                is AreaOverviewViewModel.ScreenState.Loading -> LoadingContent()
                is AreaOverviewViewModel.ScreenState.Content -> AreaOverviewScreenContent(
                    screenState = screenState,
                    contentPadding = it,
                    offlineAreaDownloader = offlineAreaDownloader,
                    displayShowOnMapButton = displayShowOnMapButton,
                    onAreaProblemsCountClicked = onAreaProblemsCountClicked,
                    onCircuitClicked = onCircuitClicked,
                    onPoiClicked = onPoiClicked
                )
                is AreaOverviewViewModel.ScreenState.UnknownArea -> AreaOverviewScreenUnknownArea()
            }
        }
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AreaOverviewScreenContent(
    screenState: AreaOverviewViewModel.ScreenState.Content,
    contentPadding: PaddingValues,
    offlineAreaDownloader: OfflineAreaDownloader,
    displayShowOnMapButton: Boolean,
    onAreaProblemsCountClicked: () -> Unit,
    onCircuitClicked: (Int) -> Unit,
    onPoiClicked: (String) -> Unit
) {
    var showGradesCountChart by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() +
                if (displayShowOnMapButton) 80.dp else 16.dp
        )
    ) {
        if (screenState.area.tags.isNotEmpty()
            || screenState.area.description != null
            || screenState.area.warning != null
        ) {
            item(key = "info") {
                SectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = spacedBy(16.dp)
                    ) {
                        if (screenState.area.tags.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = spacedBy(8.dp),
                                verticalArrangement = spacedBy(8.dp)
                            ) {
                                screenState.area.tags.forEach { tagRes ->
                                    Text(
                                        modifier = Modifier
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                shape = CircleShape
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        text = stringResource(id = tagRes),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        screenState.area.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Justify
                            )
                        }

                        screenState.area.warning?.let {
                            Row(
                                horizontalArrangement = spacedBy(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_round_warning),
                                    contentDescription = null,
                                    tint = Color.BoolderOrange
                                )

                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.BoolderOrange,
                                    textAlign = TextAlign.Justify
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        item(key = "grades-and-problems") {
            SectionContainer {
                Column {
                    GradeCountsItem(
                        gradeCounts = screenState.area.problemsCountsPerGrade,
                        showGradesCountChart = showGradesCountChart,
                        onToggleGradesCountChartVisibility = { showGradesCountChart = !showGradesCountChart }
                    )

                    BoulderProblemsItem(
                        boulderProblemsCount = screenState.area.problemsCount.toString(),
                        onAreaProblemsCountClicked = onAreaProblemsCountClicked
                    )
                }
            }
        }

        if (screenState.circuits.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            itemsIndexed(
                items = screenState.circuits,
                key = { _, item -> "circuit-${item.id}" }
            ) { index, circuit ->
                val shape = RoundedCornerShape(
                    topStart = if (index == 0) 12.dp else 0.dp,
                    topEnd = if (index == 0) 12.dp else 0.dp,
                    bottomStart = if (index == screenState.circuits.lastIndex) 12.dp else 0.dp,
                    bottomEnd = if (index == screenState.circuits.lastIndex) 12.dp else 0.dp
                )

                CircuitItem(
                    modifier = Modifier
                        .clip(shape = shape)
                        .background(color = MaterialTheme.colorScheme.surface)
                        .clickable { onCircuitClicked(circuit.id) },
                    circuit = circuit
                )
            }
        }

        if (screenState.accessesFromPoi.isNotEmpty()) {
            item(key = "access-from-pois") {
                Spacer(modifier = Modifier.height(16.dp))

                SectionContainer {
                    screenState.accessesFromPoi.forEach { accessFromPoi ->
                        AccessFromPoi(
                            accessFromPoi = accessFromPoi,
                            onPoiClicked = onPoiClicked
                        )
                    }
                }
            }
        }

        item {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                SectionContainer {
                    AreaPhotosDownloadItem(
                        areaId = screenState.area.id,
                        status = screenState.offlineAreaItemStatus,
                        offlineAreaDownloader = offlineAreaDownloader,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoulderProblemsItem(
    boulderProblemsCount: String,
    onAreaProblemsCountClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onAreaProblemsCountClicked)
            .padding(16.dp),
        horizontalArrangement = spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.area_overview_problems)
        )

        Text(text = boulderProblemsCount)
    }
}

@Composable
private fun GradeCountsItem(
    gradeCounts: Map<String, Int>,
    showGradesCountChart: Boolean,
    onToggleGradesCountChartVisibility: () -> Unit
) {
    val levelGreen = Color(red = 45, green = 161, blue = 125)

    Column(
        modifier = Modifier
            .clickable(onClick = onToggleGradesCountChartVisibility)
            .padding(16.dp),
        verticalArrangement = spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.area_overview_levels)
            )

            DegreeCountsRow(
                degreeCounts = gradeCounts,
                color = levelGreen
            )
        }

        AnimatedVisibility(visible = showGradesCountChart) {
            DegreeCountsChart(
                degreeCounts = gradeCounts,
                color = levelGreen
            )
        }
    }
}

@Composable
private fun AreaOverviewScreenUnknownArea() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.area_overview_error_cannot_display_area),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@PreviewLightDark
@Composable
private fun AreaOverviewScreenPreview(
    @PreviewParameter(AreaOverviewScreenPreviewParameterProvider::class)
    screenState: AreaOverviewViewModel.ScreenState
) {
    BoolderTheme {
        AreaOverviewScreen(
            screenState = screenState,
            offlineAreaDownloader = dummyOfflineAreaDownloader(),
            displayShowOnMapButton = true,
            onBackPressed = {},
            onSeeOnMapClicked = {},
            onAreaProblemsCountClicked = {},
            onCircuitClicked = {},
            onPoiClicked = {}
        )
    }
}

private class AreaOverviewScreenPreviewParameterProvider : PreviewParameterProvider<AreaOverviewViewModel.ScreenState> {

    override val values = sequenceOf(
        AreaOverviewViewModel.ScreenState.Loading,
        AreaOverviewViewModel.ScreenState.Content(
            area = dummyArea(),
            circuits = List(3) {
                dummyCircuit(
                    id = it,
                    color = CircuitColor.entries[it],
                    averageGrade = "${it + 1}b"
                )
            },
            accessesFromPoi = listOf(
                AccessFromPoiModel(
                    distanceInMinutes = 2,
                    transport = PoiTransport.WALKING,
                    type = PoiType.PARKING,
                    name = "Parking Rocher Canon",
                    googleUrl = ""
                ),
                AccessFromPoiModel(
                    distanceInMinutes = 15,
                    transport = PoiTransport.BIKE,
                    type = PoiType.TRAIN_STATION,
                    name = "Gare de Bois-le-Roi",
                    googleUrl = ""
                ),
                AccessFromPoiModel(
                    distanceInMinutes = 35,
                    transport = PoiTransport.WALKING,
                    type = PoiType.TRAIN_STATION,
                    name = "Gare de Bois-le-Roi",
                    googleUrl = ""
                )
            ),
            offlineAreaItemStatus = OfflineAreaItemStatus.NotDownloaded
        ),
        AreaOverviewViewModel.ScreenState.UnknownArea
    )
}
