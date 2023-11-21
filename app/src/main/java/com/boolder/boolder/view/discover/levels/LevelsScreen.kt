package com.boolder.boolder.view.discover.levels

import androidx.annotation.StringRes
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
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.LoadingScreen
import com.boolder.boolder.view.compose.degreecounts.DegreeCountsRow
import com.boolder.boolder.view.discover.composable.AreaThumbnailsRow
import com.boolder.boolder.view.discover.discover.DiscoverHeaderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LevelsScreen(
    screenState: LevelsViewModel.ScreenState,
    onBackPressed: () -> Unit,
    onMoreBeginnerFriendlyAreasClicked: () -> Unit,
    onAreaClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = DiscoverHeaderItem.PER_LEVEL.textRes))
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
                is LevelsViewModel.ScreenState.Loading -> LoadingScreen()
                is LevelsViewModel.ScreenState.Content -> LevelsScreenContent(
                    modifier = Modifier.padding(it),
                    screenState = screenState,
                    onMoreBeginnerFriendlyAreasClicked = onMoreBeginnerFriendlyAreasClicked,
                    onAreaClicked = onAreaClicked
                )
            }
        }
    )
}

@Composable
private fun LevelsScreenContent(
    screenState: LevelsViewModel.ScreenState.Content,
    onMoreBeginnerFriendlyAreasClicked: () -> Unit,
    onAreaClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {
        item {
            SectionTitle(
                modifier = Modifier.padding(bottom = 8.dp),
                textRes = R.string.top_areas_levels_beginner_friendly,
                onClick = onMoreBeginnerFriendlyAreasClicked
            )
        }

        item {
            AreaThumbnailsRow(
                areas = screenState.beginnerAreas,
                onAreaClicked = onAreaClicked
            )
        }

        item {
            SectionTitle(
                modifier = Modifier.padding(vertical = 8.dp),
                textRes = R.string.top_areas_levels_all_areas
            )
        }

        items(screenState.allAreas) {
            AreaLevelItem(
                area = it,
                onClick = { onAreaClicked(it.id) }
            )
        }
    }
}

@Composable
private fun SectionTitle(
    @StringRes textRes: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        onClick?.let {
            Icon(
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .clickable(onClick = it)
                    .padding(8.dp),
                painter = painterResource(id = R.drawable.ic_more_horiz),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun AreaLevelItem(
    area: Area,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = area.name
        )

        DegreeCountsRow(
            degreeCounts = area.problemsCountsPerGrade,
            color = Color(red = 45, green = 161, blue = 125)
        )
    }
}

@Preview
@Composable
private fun LevelsScreenPreview(
    @PreviewParameter(LevelsScreenPreviewParameterProvider::class)
    screenState: LevelsViewModel.ScreenState
) {
    BoolderTheme {
        LevelsScreen(
            screenState = screenState,
            onBackPressed = {},
            onMoreBeginnerFriendlyAreasClicked = {},
            onAreaClicked = {}
        )
    }
}

private class LevelsScreenPreviewParameterProvider : PreviewParameterProvider<LevelsViewModel.ScreenState> {
    private val allAreas = List(20) { dummyArea(id = it, name = "Area $it") }
    private val beginnerAreas = listOf(
        "Canche aux Merciers",
        "Franchard Isatis",
        "Rocher Canon"
    ).mapIndexed { index, name ->
        dummyArea(id = 100 + index, name = name)
    }

    override val values = sequenceOf(
        LevelsViewModel.ScreenState.Loading,
        LevelsViewModel.ScreenState.Content(
            beginnerAreas = beginnerAreas,
            allAreas = allAreas
        )
    )
}
