package com.boolder.boolder.view.discover.levels.beginner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.utils.previewgenerator.dummyCircuit
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BeginnerLevelsScreen(
    screenState: BeginnerLevelsViewModel.ScreenState,
    onBackPressed: () -> Unit,
    onAreaClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.top_areas_levels_beginner_friendly))
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
                is BeginnerLevelsViewModel.ScreenState.Loading -> LoadingScreen()
                is BeginnerLevelsViewModel.ScreenState.Content -> BeginnerLevelsScreenContent(
                    modifier = Modifier.padding(it),
                    screenState = screenState,
                    onAreaClicked = onAreaClicked
                )
            }
        }
    )
}

@Composable
private fun BeginnerLevelsScreenContent(
    screenState: BeginnerLevelsViewModel.ScreenState.Content,
    onAreaClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {
        item {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(id = R.string.top_areas_levels_beginner_friendly_intro),
                color = Color.Gray
            )
        }

        items(screenState.areasWithCircuits) { (area, circuits) ->
            AreaWithBeginnerCircuitsItem(
                area = area,
                circuits = circuits,
                onClick = { onAreaClicked(area.id) }
            )
        }
    }
}

@Composable
private fun AreaWithBeginnerCircuitsItem(
    area: Area,
    circuits: List<Circuit>,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = area.name
        )

        Row(
            horizontalArrangement = spacedBy(8.dp)
        ) {
            circuits.forEach {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = colorResource(id = it.color.colorRes),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Preview
@Composable
private fun BeginnerLevelsScreenPreview(
    @PreviewParameter(BeginnerLevelsScreenParameterProvider::class)
    screenState: BeginnerLevelsViewModel.ScreenState
) {
    BoolderTheme {
        BeginnerLevelsScreen(
            screenState = screenState,
            onBackPressed = {},
            onAreaClicked = {}
        )
    }
}

private class BeginnerLevelsScreenParameterProvider : PreviewParameterProvider<BeginnerLevelsViewModel.ScreenState> {
    override val values = sequenceOf(
        BeginnerLevelsViewModel.ScreenState.Loading,
        BeginnerLevelsViewModel.ScreenState.Content(
            areasWithCircuits = listOf(
                dummyArea(id = 0, name = "Canche aux Merciers") to listOf(
                    dummyCircuit(color = CircuitColor.PURPLE),
                    dummyCircuit(color = CircuitColor.YELLOW),
                    dummyCircuit(color = CircuitColor.ORANGE)
                ),
                dummyArea(id = 0, name = "Franchard Isatis") to listOf(
                    dummyCircuit(color = CircuitColor.YELLOW),
                    dummyCircuit(color = CircuitColor.ORANGE)
                ),
                dummyArea(id = 0, name = "Rocher Canon") to listOf(
                    dummyCircuit(color = CircuitColor.PURPLE),
                    dummyCircuit(color = CircuitColor.YELLOW)
                ),
                dummyArea(id = 0, name = "Apremont Bizons") to listOf(
                    dummyCircuit(color = CircuitColor.YELLOW),
                    dummyCircuit(color = CircuitColor.ORANGE)
                ),
                dummyArea(id = 0, name = "Beauvais Nainville") to listOf(
                    dummyCircuit(color = CircuitColor.YELLOW),
                    dummyCircuit(color = CircuitColor.ORANGE)
                ),
                dummyArea(id = 0, name = "Rocher du Potala") to listOf(
                    dummyCircuit(color = CircuitColor.YELLOW)
                ),
                dummyArea(id = 0, name = "Apremont Butte aux Dames") to listOf(
                    dummyCircuit(color = CircuitColor.YELLOW)
                ),
                dummyArea(id = 0, name = "Buthiers Piscine") to listOf(
                    dummyCircuit(color = CircuitColor.ORANGE)
                )
            )
        )
    )
}
