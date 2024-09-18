package com.boolder.boolder.view.map.areadownload

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.offline.OfflineAreaDownloader
import com.boolder.boolder.offline.OfflineClusterDownloader
import com.boolder.boolder.offline.dummyOfflineAreaDownloader
import com.boolder.boolder.offline.dummyOfflineClusterDownloader
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.utils.previewgenerator.dummyOfflineAreaItem
import com.boolder.boolder.view.compose.BoolderOrange
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.shimmerLoading
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import com.boolder.boolder.view.offlinephotos.model.OfflineClusterItem
import com.boolder.boolder.view.offlinephotos.model.OfflineClusterItemStatus

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AreaDownloadLayout(
    screenState: AreaDownloadViewModel.ScreenState,
    offlineClusterDownloader: OfflineClusterDownloader,
    offlineAreaDownloader: OfflineAreaDownloader,
    scrollOffset: Float,
    onDismissHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topInset * scrollOffset)
                .background(color = Color.Transparent)
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(color = MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
                text = stringResource(id = R.string.area_download_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(
                modifier = Modifier.graphicsLayer { alpha = scrollOffset },
                color = MaterialTheme.colorScheme.outline
            )

            val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val itemShape = RoundedCornerShape(16.dp)

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp + bottomInset
                ),
                verticalArrangement = spacedBy(8.dp)
            ) {
                item {
                    val clusterItemModifier = Modifier
                        .padding(bottom = 16.dp)
                        .clip(shape = itemShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = itemShape
                        )

                    screenState.content?.clusterItem
                        ?.let {
                            ClusterItem(
                                modifier = clusterItemModifier,
                                clusterItem = it,
                                offlineClusterDownloader = offlineClusterDownloader
                            )
                        }
                        ?: LoadingAreaItem(
                            modifier = clusterItemModifier
                        )
                }

                if (screenState.content?.showDownloadHint == true) {
                    item(key = "download_hint") {
                        DownloadHint(
                            modifier = Modifier.animateItemPlacement(),
                            shape = itemShape,
                            onDismissClicked = onDismissHint
                        )
                    }
                }

                screenState.content?.areaItems
                    ?.let { areaItems ->
                        items(
                            items = areaItems,
                            key = { it.area.id }
                        ) { item ->
                            AreaItem(
                                modifier = Modifier
                                    .clip(shape = itemShape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = itemShape
                                    ),
                                offlineAreaItem = item,
                                offlineAreaDownloader = offlineAreaDownloader
                            )
                        }
                    }
                    ?: items(count = screenState.areasCount) {
                        LoadingAreaItem(
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = itemShape
                            )
                        )
                    }
            }
        }
    }
}

@Composable
private fun LoadingAreaItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
    offlineAreaDownloader: OfflineAreaDownloader,
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
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

@Composable
private fun ClusterItem(
    clusterItem: OfflineClusterItem,
    offlineClusterDownloader: OfflineClusterDownloader,
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable {
                when (clusterItem.status) {
                    OfflineClusterItemStatus.NOT_DOWNLOADED -> offlineClusterDownloader.onDownloadCluster()
                    OfflineClusterItemStatus.DOWNLOADING -> showCancelDialog = true
                    OfflineClusterItemStatus.DOWNLOADED -> showDeleteDialog = true
                }
            }
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.area_download_sheet_all_areas_in_cluster, clusterItem.name),
            color = MaterialTheme.colorScheme.onSurface
        )

        ClusterItemIcon(item = clusterItem)

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
                            offlineClusterDownloader.onCancelClusterDownload()
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
                            offlineClusterDownloader.onDeleteClusterPhotos()
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
}

@Composable
private fun ClusterItemIcon(item: OfflineClusterItem) {
    when (item.status) {
        OfflineClusterItemStatus.NOT_DOWNLOADED -> Icon(
            painter = painterResource(id = R.drawable.ic_download_for_offline),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )

        OfflineClusterItemStatus.DOWNLOADING -> {
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
                    .graphicsLayer { rotationZ = rotationAngle }
            )
        }

        OfflineClusterItemStatus.DOWNLOADED -> Icon(
            painter = painterResource(id = R.drawable.ic_download_done),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DownloadHint(
    shape: Shape,
    onDismissClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.area_download_sheet_hint_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onDismissClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f)
                )
            }
        }

        Text(
            text = stringResource(id = R.string.area_download_sheet_hint_message),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f)
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
            offlineClusterDownloader = dummyOfflineClusterDownloader(),
            offlineAreaDownloader = dummyOfflineAreaDownloader(),
            scrollOffset = 0f,
            onDismissHint = {}
        )
    }
}

private class AreaDownloadLayoutPreviewParameterProvider : PreviewParameterProvider<AreaDownloadViewModel.ScreenState> {
    private val loadingState = AreaDownloadViewModel.ScreenState(
        areasCount = 3,
        content = null
    )

    private val contents = listOf(
        OfflineAreaItemStatus.NotDownloaded,
        OfflineAreaItemStatus.Downloading(progress = .6f, progressDetail = "6/10"),
        OfflineAreaItemStatus.Downloaded(folderSize = "33 MB"),
    ).map {
        AreaDownloadViewModel.ScreenState(
            areasCount = 3,
            content = AreaDownloadViewModel.Content(
                clusterItem = OfflineClusterItem(
                    name = "Franchard",
                    status = OfflineClusterItemStatus.DOWNLOADING
                ),
                areaItems = listOf(
                    dummyOfflineAreaItem(
                        area = dummyArea(
                            id = 0,
                            name = "Franchard Sablons"
                        ),
                        status = OfflineAreaItemStatus.NotDownloaded
                    ),
                    dummyOfflineAreaItem(
                        area = dummyArea(
                            id = 1,
                            name = "Franchard Cuisinière"
                        ),
                        status = OfflineAreaItemStatus.Downloading(progress = .3f, progressDetail = "3/10")
                    ),
                    dummyOfflineAreaItem(
                        area = dummyArea(
                            id = 2,
                            name = "Franchard Hautes Plaines"
                        ),
                        status = OfflineAreaItemStatus.Downloaded(folderSize = "40MB")
                    )
                ),
                showDownloadHint = true
            )
        )
    }

    override val values = listOf(loadingState)
        .plus(contents)
        .asSequence()
}
