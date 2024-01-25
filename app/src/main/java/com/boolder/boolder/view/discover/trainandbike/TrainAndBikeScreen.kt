package com.boolder.boolder.view.discover.trainandbike

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.AreaBikeRoute
import com.boolder.boolder.domain.model.TrainStationPoi
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.LoadingScreen
import com.boolder.boolder.view.discover.discover.DiscoverHeaderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrainAndBikeScreen(
    screenState: TrainAndBikeViewModel.ScreenState,
    onBackPressed: () -> Unit,
    onOpenGoogleMapsUrl: (String) -> Unit,
    onAreaClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = DiscoverHeaderItem.TRAIN_AND_BIKE.textRes))
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
        content = {
            when (screenState) {
                is TrainAndBikeViewModel.ScreenState.Loading -> LoadingScreen()
                is TrainAndBikeViewModel.ScreenState.Content -> TrainAndBikeScreenContent(
                    screenState = screenState,
                    contentPadding = it,
                    onOpenGoogleMapsUrl = onOpenGoogleMapsUrl,
                    onAreaClicked = onAreaClicked
                )
            }
        }
    )
}

@Composable
private fun TrainAndBikeScreenContent(
    screenState: TrainAndBikeViewModel.ScreenState.Content,
    contentPadding: PaddingValues,
    onOpenGoogleMapsUrl: (String) -> Unit,
    onAreaClicked: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = spacedBy(8.dp)
    ) {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.top_areas_train_and_bike_intro),
                color = Color.Gray
            )
        }

        screenState.poisMap.entries.forEach { (trainStation, areaBikeRoutes) ->
            item {
                TrainStationPoiItem(
                    item = trainStation,
                    onDirectionIconClicked = { onOpenGoogleMapsUrl(trainStation.googleUrl) }
                )
            }

            items(areaBikeRoutes) { areaBikeRoute ->
                AreaBikeRouteItem(
                    item = areaBikeRoute,
                    onClick = { onAreaClicked(areaBikeRoute.areaId) }
                )
            }
        }
    }
}

@Composable
private fun TrainStationPoiItem(
    item: TrainStationPoi,
    onDirectionIconClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = item.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Icon(
            modifier = Modifier
                .clip(shape = CircleShape)
                .clickable(onClick = onDirectionIconClicked)
                .padding(8.dp),
            painter = painterResource(id = R.drawable.ic_directions),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AreaBikeRouteItem(
    item: AreaBikeRoute,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = item.areaName
        )

        Text(
            text = stringResource(id = R.string.poi_distance_in_min, item.bikingTime)
        )
    }
}

@Preview
@Composable
private fun TrainAndBikeScreenPreview(
    @PreviewParameter(TrainAndBikeScreenPreviewParameterProvider::class)
    screenState: TrainAndBikeViewModel.ScreenState
) {
    BoolderTheme {
        TrainAndBikeScreen(
            screenState = screenState,
            onBackPressed = {},
            onOpenGoogleMapsUrl = {},
            onAreaClicked = {}
        )
    }
}

private class TrainAndBikeScreenPreviewParameterProvider : PreviewParameterProvider<TrainAndBikeViewModel.ScreenState> {
    private val poisMap = mapOf(
        TrainStationPoi(
            name = "Gare de Bois-le-Roi",
            googleUrl = "https://goo.gl/maps/dummy.url",
        ) to listOf(
            AreaBikeRoute(
                areaId = 0,
                areaName = "Rocher Canon",
                bikingTime = 15
            ),
            AreaBikeRoute(
                areaId = 0,
                areaName = "Rocher Saint-Germain Est",
                bikingTime = 20
            )
        ),
        TrainStationPoi(
            name = "Gare de Fontainebleau-Avon",
            googleUrl = "https://goo.gl/maps/dummy.url"
        ) to listOf(
            AreaBikeRoute(
                areaId = 0,
                areaName = "Mont Ussy",
                bikingTime = 10
            ),
            AreaBikeRoute(
                areaId = 0,
                areaName = "Le Calvaire",
                bikingTime = 10
            ),
            AreaBikeRoute(
                areaId = 0,
                areaName = "Rocher d'Avon",
                bikingTime = 15
            ),
            AreaBikeRoute(
                areaId = 0,
                areaName = "Roche d'Hercule",
                bikingTime = 15
            ),
            AreaBikeRoute(
                areaId = 0,
                areaName = "Mont Aigu",
                bikingTime = 25
            ),
            AreaBikeRoute(
                areaId = 0,
                areaName = "Rocher de Bouligny",
                bikingTime = 30
            ),
            AreaBikeRoute(
                areaId = 0,
                areaName = "Rocher des Demoiselles",
                bikingTime = 35
            )
        ),
        TrainStationPoi(
            name = "Gare de Montigny-sur-Loing",
            googleUrl = "https://goo.gl/maps/dummy.url"
        ) to listOf(
            AreaBikeRoute(
                areaId = 0,
                areaName = "Restant du Long Rocher",
                bikingTime = 20
            ),
            AreaBikeRoute(
                areaId = 0,
                areaName = "Restant du Long Rocher Sud",
                bikingTime = 20
            )
        )
    )

    override val values = sequenceOf(
        TrainAndBikeViewModel.ScreenState.Loading,
        TrainAndBikeViewModel.ScreenState.Content(
            poisMap = poisMap
        )
    )
}
