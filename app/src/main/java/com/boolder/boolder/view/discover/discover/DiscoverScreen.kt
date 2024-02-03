package com.boolder.boolder.view.discover.discover

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.LoadingScreen
import com.boolder.boolder.view.discover.composable.AreaThumbnailsRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    screenState: DiscoverViewModel.ScreenState,
    onDiscoverHeaderItemClicked: (DiscoverHeaderItem) -> Unit,
    onAreaClicked: (Int) -> Unit,
    onRateAppClicked: () -> Unit,
    onContributeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .padding(bottom = dimensionResource(id = R.dimen.height_bottom_nav_bar))
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.tab_discover))
                }
            )
        },
        content = {
            when (screenState) {
                is DiscoverViewModel.ScreenState.Loading -> LoadingScreen()
                is DiscoverViewModel.ScreenState.Content -> DiscoverScreenContent(
                    screenState = screenState,
                    contentPadding = it,
                    onDiscoverHeaderItemClicked = onDiscoverHeaderItemClicked,
                    onAreaClicked = onAreaClicked,
                    onRateAppClicked = onRateAppClicked,
                    onContributeClicked = onContributeClicked
                )
            }
        }
    )
}

@Composable
private fun DiscoverScreenContent(
    screenState: DiscoverViewModel.ScreenState.Content,
    contentPadding: PaddingValues,
    onDiscoverHeaderItemClicked: (DiscoverHeaderItem) -> Unit,
    onAreaClicked: (Int) -> Unit,
    onRateAppClicked: () -> Unit,
    onContributeClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = 16.dp
        ),
        verticalArrangement = spacedBy(8.dp)
    ) {
        item {
            DiscoverHeader(onItemClicked = onDiscoverHeaderItemClicked)
        }

        item {
            DiscoverSectionTitle(text = stringResource(id = R.string.discover_section_popular_areas))
        }

        item {
            AreaThumbnailsRow(
                areas = screenState.popularAreas,
                onAreaClicked = onAreaClicked
            )
        }

        item {
            DiscoverSectionTitle(text = stringResource(id = R.string.discover_section_all_areas))
        }

        items(screenState.allAreas) {
            AreaItem(
                area = it,
                onClick = onAreaClicked
            )
        }

        item {
            DiscoverSectionTitle(text = stringResource(id = R.string.discover_section_support))
        }

        item {
            SupportItem(
                text = stringResource(id = R.string.discover_support_rate),
                iconRes = R.drawable.ic_star_outline,
                onClick = onRateAppClicked
            )
        }

        item {
            SupportItem(
                text = stringResource(id = R.string.discover_support_contribute),
                iconRes = R.drawable.ic_add_circle_outline,
                onClick = onContributeClicked
            )
        }
    }
}

@Composable
private fun DiscoverHeader(onItemClicked: (DiscoverHeaderItem) -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {
        DiscoverHeaderRow(
            startItem = DiscoverHeaderItem.BEGINNER_GUIDE,
            endItem = DiscoverHeaderItem.PER_LEVEL,
            onItemClicked = onItemClicked
        )
        DiscoverHeaderRow(
            startItem = DiscoverHeaderItem.DRIES_FAST,
            endItem = DiscoverHeaderItem.TRAIN_AND_BIKE,
            onItemClicked = onItemClicked
        )
    }
}

@Composable
private fun DiscoverHeaderRow(
    startItem: DiscoverHeaderItem,
    endItem: DiscoverHeaderItem,
    onItemClicked: (DiscoverHeaderItem) -> Unit
) {
    Row(
        horizontalArrangement = spacedBy(8.dp)
    ) {
        DiscoverHeaderItem(
            modifier = Modifier.weight(1f),
            item = startItem,
            onClick = { onItemClicked(startItem) }
        )
        DiscoverHeaderItem(
            modifier = Modifier.weight(1f),
            item = endItem,
            onClick = { onItemClicked(endItem) }
        )
    }
}

@Composable
private fun DiscoverHeaderItem(
    item: DiscoverHeaderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(
                brush = verticalGradient(
                    listOf(
                        Color(item.backgroundStartColor),
                        Color(item.backgroundEndColor)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item.iconRes?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                tint = Color.White
            )
        }

        Text(
            text = stringResource(id = item.textRes).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DiscoverSectionTitle(text: String) {
    Text(
        modifier = Modifier.padding(16.dp),
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = Color.Black,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SupportItem(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null
        )

        Text(
            text = text
        )
    }
}

@Composable
private fun AreaItem(
    area: Area,
    onClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .clickable { onClick(area.id) }
            .padding(12.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = area.name,
            color = Color.Black
        )

        Text(
            text = area.problemsCount.toString(),
            color = Color.Gray
        )
    }
}

@Preview
@Composable
private fun DiscoverScreenPreview(
    @PreviewParameter(DiscoverScreenPreviewParameterProvider::class)
    screenState: DiscoverViewModel.ScreenState
) {
    BoolderTheme {
        DiscoverScreen(
            screenState = screenState,
            onDiscoverHeaderItemClicked = {},
            onAreaClicked = {},
            onRateAppClicked = {},
            onContributeClicked = {}
        )
    }
}

private class DiscoverScreenPreviewParameterProvider : PreviewParameterProvider<DiscoverViewModel.ScreenState> {
    private val dummyAreas = List(10) {
        dummyArea(
            id = it,
            name = "Area $it",
            problemsCount = it * 100
        )
    }

    override val values = sequenceOf(
        DiscoverViewModel.ScreenState.Loading,
        DiscoverViewModel.ScreenState.Content(
            allAreas = dummyAreas,
            popularAreas = dummyAreas
        )
    )
}
