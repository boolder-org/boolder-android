package com.boolder.boolder.view.discover.driesfast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.view.compose.BoolderOrange
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.BoolderYellow
import com.boolder.boolder.view.compose.LoadingScreen
import com.boolder.boolder.view.discover.composable.AreaThumbnailsRow
import com.boolder.boolder.view.discover.discover.DiscoverHeaderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DriesFastScreen(
    screenState: DriesFastViewModel.ScreenState,
    onBackPressed: () -> Unit,
    onAreaClicked: (Int) -> Unit,
    onBleauWeatherClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = DiscoverHeaderItem.DRIES_FAST.textRes))
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
                is DriesFastViewModel.ScreenState.Loading -> LoadingScreen()
                is DriesFastViewModel.ScreenState.Content -> DriesFastScreenContent(
                    modifier = Modifier.padding(it),
                    screenState = screenState,
                    onAreaClicked = onAreaClicked,
                    onBleauWeatherClicked = onBleauWeatherClicked
                )
            }
        }
    )
}

@Composable
private fun DriesFastScreenContent(
    screenState: DriesFastViewModel.ScreenState.Content,
    onAreaClicked: (Int) -> Unit,
    onBleauWeatherClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 16.dp),
        verticalArrangement = spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.top_areas_dry_fast_description),
            color = Color.Gray
        )

        AreaThumbnailsRow(
            areas = screenState.areas,
            onAreaClicked = onAreaClicked
        )

        WarningItem(
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        UsefulLinkItem(
            modifier = Modifier.padding(horizontal = 16.dp),
            onBleauWeatherClicked = onBleauWeatherClicked
        )
    }
}

@Composable
private fun WarningItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Color.BoolderYellow.copy(alpha = .2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        horizontalArrangement = spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_outline_report),
            contentDescription = null,
            tint = Color.BoolderOrange
        )

        Text(
            text = stringResource(id = R.string.top_areas_dry_fast_warning),
            color = Color.BoolderOrange
        )
    }
}

@Composable
private fun UsefulLinkItem(
    onBleauWeatherClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usefulLinkStr = stringResource(id = R.string.top_areas_dry_fast_useful_link)

    ClickableText(
        modifier = modifier,
        text = buildAnnotatedString {
            append(usefulLinkStr)
            append(" ")
            withStyle(
                MaterialTheme.typography.bodyLarge.toSpanStyle()
                    .copy(color = MaterialTheme.colorScheme.primary)
            ) {
                append("Bleau Météo")
            }
        },
        style = MaterialTheme.typography.bodyLarge
            .copy(color = Color.Gray),
        onClick = { offset ->
            if (offset > usefulLinkStr.length) onBleauWeatherClicked()
        }
    )
}

@Preview
@Composable
private fun DriesFastScreenPreview(
    @PreviewParameter(DriesFastScreenPreviewParameterProvider::class)
    screenState: DriesFastViewModel.ScreenState
) {
    BoolderTheme {
        DriesFastScreen(
            screenState = screenState,
            onBackPressed = {},
            onAreaClicked = {},
            onBleauWeatherClicked = {}
        )
    }
}

private class DriesFastScreenPreviewParameterProvider : PreviewParameterProvider<DriesFastViewModel.ScreenState> {
    override val values = sequenceOf(
        DriesFastViewModel.ScreenState.Loading,
        DriesFastViewModel.ScreenState.Content(
            areas = List(10) {
                dummyArea(
                    id = it,
                    name = "Area $it",
                    problemsCount = it * 100
                )
            }
        )
    )
}
