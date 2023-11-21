package com.boolder.boolder.view.offlinephotos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.offlinephotos.composable.OfflinePhotosAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus

@Composable
fun OfflinePhotosScreen(
    screenState: OfflinePhotosViewModel.ScreenState,
    onDownloadAreaClicked: (Int) -> Unit,
    onDownloadTerminated: (Int) -> Unit,
    onCancelDownload: (Int) -> Unit,
    onDeleteAreaClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val internalModifier = modifier
        .fillMaxSize()
        .background(color = Color.White)

    if (screenState.items.isEmpty()) {
        OfflinePhotosScreenLoading(modifier = internalModifier)
    } else {
        OfflinePhotosScreenContent(
            modifier = internalModifier,
            screenState = screenState,
            onDownloadAreaClicked = onDownloadAreaClicked,
            onDownloadTerminated = onDownloadTerminated,
            onCancelDownload = onCancelDownload,
            onDeleteAreaClicked = onDeleteAreaClicked
        )
    }
}

@Composable
private fun OfflinePhotosScreenLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun OfflinePhotosScreenContent(
    screenState: OfflinePhotosViewModel.ScreenState,
    onDownloadAreaClicked: (Int) -> Unit,
    onDownloadTerminated: (Int) -> Unit,
    onCancelDownload: (Int) -> Unit,
    onDeleteAreaClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = systemBarsInsets.calculateTopPadding() + 16.dp,
            end = 16.dp,
            bottom = systemBarsInsets.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = spacedBy(16.dp)
    ) {
        items(
            items = screenState.items,
            key = { it.area.id }
        ) {
            OfflinePhotosAreaItem(
                areaName = it.area.name,
                status = it.status,
                onDownloadClicked = { onDownloadAreaClicked(it.area.id) },
                onDownloadTerminated = { onDownloadTerminated(it.area.id) },
                onCancelDownload = { onCancelDownload(it.area.id) },
                onDeleteClicked = { onDeleteAreaClicked(it.area.id) }
            )
        }
    }
}

@Preview
@Composable
private fun OfflinePhotosScreenPreview(
    @PreviewParameter(OfflinePhotosScreenPreviewParameterProvider::class)
    screenState: OfflinePhotosViewModel.ScreenState
) {
    BoolderTheme {
        OfflinePhotosScreen(
            modifier = Modifier.background(color = Color.White),
            screenState = screenState,
            onDownloadAreaClicked = {},
            onDownloadTerminated = {},
            onCancelDownload = {},
            onDeleteAreaClicked = {}
        )
    }
}

private class OfflinePhotosScreenPreviewParameterProvider :
    PreviewParameterProvider<OfflinePhotosViewModel.ScreenState> {

    private val content = OfflinePhotosViewModel.ScreenState(
        items = listOf(
            area(16, "91.1"),
            area(10, "95.2"),
            area(7, "Apremont"),
            area(46, "Apremont Bizons"),
            area(48, "Apremont Butte aux Dames"),
            area(63, "Apremont Désert"),
            area(62, "Apremont Envers"),
            area(69, "Apremont Est"),
            area(20, "Apremont Ouest"),
            area(49, "Apremont Solitude"),
            area(29, "Beauvais Nainville"),
            area(21, "Bois Rond"),
            area(78, "Buthiers Canard"),
            area(23, "Buthiers Piscine"),
            area(77, "Buthiers Tennis"),
            area(13, "Canche aux Merciers")
        ).mapIndexed { index, area ->
            OfflineAreaItem(
                area = area,
                status = when {
                    index < 4 -> OfflineAreaItemStatus.Downloaded(folderSize = "50 MB")
                    index < 5 -> OfflineAreaItemStatus.Downloading(areaId = 48)
                    else -> OfflineAreaItemStatus.NotDownloaded
                }
            )
        }
    )

    private fun area(id: Int, name: String) = Area(
        id = id,
        name = name,
        description = "",
        southWestLat = 0f,
        southWestLon = 0f,
        northEastLat = 0f,
        northEastLon = 0f,
        problemsCount = 0,
        problemsCountsPerGrade = emptyMap()
    )

    override val values = sequenceOf(
        OfflinePhotosViewModel.ScreenState(items = emptyList()),
        content
    )

}
