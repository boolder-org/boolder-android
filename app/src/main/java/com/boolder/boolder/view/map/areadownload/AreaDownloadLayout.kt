package com.boolder.boolder.view.map.areadownload

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.offline.dummyOfflineAreaDownloader
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.utils.previewgenerator.dummyOfflineAreaItem
import com.boolder.boolder.view.compose.BoolderOrange
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.shimmerLoading
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus

@Composable
internal fun AreaDownloadLayout(
    screenState: AreaDownloadViewModel.ScreenState,
    offlineAreaDownloader: OfflineAreaDownloader,
    modifier: Modifier = Modifier
) {
    val displayNearbyItems = when {
        screenState.loadingNearbyItemsCount != null -> screenState.loadingNearbyItemsCount > 0
        screenState.content != null -> screenState.content.nearbyAreaItems.isNotEmpty()
        else -> false
    }

    AreaDownloadScaffold(
        modifier = modifier,
        displayNearbyItems = displayNearbyItems,
        currentArea = {
            screenState.loadingNearbyItemsCount?.let { LoadingAreaItem() }

            screenState.content?.let {
                AreaItem(
                    offlineAreaItem = it.offlineAreaItem,
                    offlineAreaDownloader = offlineAreaDownloader
                )
            }
        },
        nearbyAreas = {
            screenState.loadingNearbyItemsCount?.let {
                repeat(it) { index ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    LoadingAreaItem()
                }
            }

            screenState.content?.let {
                it.nearbyAreaItems.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    AreaItem(
                        offlineAreaItem = item,
                        offlineAreaDownloader = offlineAreaDownloader
                    )
                }
            }
        }
    )
}

@Composable
private fun AreaDownloadScaffold(
    displayNearbyItems: Boolean,
    currentArea: @Composable () -> Unit,
    nearbyAreas: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.area_download_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        SectionColumn { currentArea() }

        if (displayNearbyItems) {
            Text(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 8.dp),
                text = stringResource(id = R.string.area_download_sheet_nearby_areas),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            SectionColumn { nearbyAreas() }
        }
    }
}

@Composable
private fun SectionColumn(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .clip(shape = shape)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = shape)
    ) {
        content()
    }
}

@Composable
private fun LoadingAreaItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerLoading()
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerLoading()
        )
    }
}

@Composable
private fun AreaItem(
    offlineAreaItem: OfflineAreaItem,
    offlineAreaDownloader: OfflineAreaDownloader
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .clickable {
                when (offlineAreaItem.status) {
                    is OfflineAreaItemStatus.NotDownloaded -> offlineAreaDownloader.onDownloadArea(
                        offlineAreaItem.area.id
                    )

                    is OfflineAreaItemStatus.Downloading -> showCancelDialog = true
                    is OfflineAreaItemStatus.Downloaded -> showDeleteDialog = true
                }
            }
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = offlineAreaItem.area.name,
            color = MaterialTheme.colorScheme.onSurface
        )

        AreaItemIcon(item = offlineAreaItem)
    }

    if (showCancelDialog) {
        AlertDialog(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_error_outline),
                    contentDescription = null,
                    tint = Color.BoolderOrange
                )
            },
            title = { Text(text = stringResource(id = R.string.photos_download_cancellation_dialog_title)) },
            text = { Text(text = stringResource(id = R.string.photos_download_cancellation_dialog_text)) },
            onDismissRequest = { showCancelDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        offlineAreaDownloader.onCancelAreaDownload(offlineAreaItem.area.id)
                        showCancelDialog = false
                    },
                    content = { Text(text = stringResource(id = R.string.photos_download_cancellation_dialog_confirm_button)) }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false },
                    content =  { Text(text = stringResource(id = R.string.photos_download_cancellation_dialog_cancel_button)) }
                )
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_error_outline),
                    contentDescription = null,
                    tint = Color.BoolderOrange
                )
            },
            title = { Text(text = stringResource(id = R.string.photos_download_deletion_dialog_title)) },
            text = { Text(text = stringResource(id = R.string.photos_download_deletion_dialog_text)) },
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        offlineAreaDownloader.onDeleteAreaPhotos(offlineAreaItem.area.id)
                    },
                    content = { Text(text = stringResource(id = R.string.photos_download_deletion_dialog_confirm_button)) }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    content = { Text(text = stringResource(id = R.string.photos_download_deletion_dialog_cancel_button)) }
                )
            }
        )
    }
}

@Composable
private fun AreaItemIcon(item: OfflineAreaItem) {
    when (item.status) {
        is OfflineAreaItemStatus.NotDownloaded -> Icon(
            painter = painterResource(id = R.drawable.ic_download_for_offline),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        is OfflineAreaItemStatus.Downloading -> {
            val infiniteTransition = rememberInfiniteTransition("rotationAngle")
            val rotationAngle by infiniteTransition.animateFloat(
                label = "rotationAngle",
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    tween(
                        durationMillis = 1_500,
                        easing = LinearEasing
                    )
                )
            )

            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = rotationAngle },
                progress = { item.status.progress.coerceAtLeast(.05f) }
            )
        }
        is OfflineAreaItemStatus.Downloaded -> Icon(
            painter = painterResource(id = R.drawable.ic_download_done),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@PreviewLightDark
@Composable
private fun AreaDownloadLayoutPreview(
    @PreviewParameter(AreaDownloadLayoutPreviewParameterProvider::class)
    screenState: AreaDownloadViewModel.ScreenState
) {
    BoolderTheme {
        AreaDownloadLayout(
            screenState = screenState,
            offlineAreaDownloader = dummyOfflineAreaDownloader()
        )
    }
}

private class AreaDownloadLayoutPreviewParameterProvider : PreviewParameterProvider<AreaDownloadViewModel.ScreenState> {
    private val loadingState = AreaDownloadViewModel.ScreenState(
        loadingNearbyItemsCount = 3,
        content = null
    )

    private val contents = listOf(
        OfflineAreaItemStatus.NotDownloaded,
        OfflineAreaItemStatus.Downloading(progress = .6f, progressDetail = "6/10"),
        OfflineAreaItemStatus.Downloaded(folderSize = "33 MB"),
    ).map {
        AreaDownloadViewModel.ScreenState(
            loadingNearbyItemsCount = null,
            content = AreaDownloadViewModel.Content(
                offlineAreaItem = dummyOfflineAreaItem(
                    area = dummyArea(name = "Franchard Isatis"),
                    status = it
                ),
                nearbyAreaItems = listOf(
                    dummyOfflineAreaItem(
                        area = dummyArea(name = "Franchard Sablons"),
                        status = OfflineAreaItemStatus.NotDownloaded
                    ),
                    dummyOfflineAreaItem(
                        area = dummyArea(name = "Franchard Cuisinière"),
                        status = OfflineAreaItemStatus.Downloading(progress = .3f, progressDetail = "3/10")
                    ),
                    dummyOfflineAreaItem(
                        area = dummyArea(name = "Franchard Hautes Plaines"),
                        status = OfflineAreaItemStatus.Downloaded(folderSize = "40MB")
                    )
                )
            )
        )
    }

    override val values = listOf(loadingState)
        .plus(contents)
        .asSequence()
}
