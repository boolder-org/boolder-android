package com.boolder.boolder.view.areadetails.areaoverview.composable.download

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.boolder.boolder.view.compose.BoolderOrange
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import java.util.Locale

@Composable
internal fun AreaPhotosDownloadItem(
    areaId: Int,
    status: OfflineAreaItemStatus,
    offlineAreaDownloader: OfflineAreaDownloader,
    modifier: Modifier = Modifier
) {
    var showDeletionDialog by remember { mutableStateOf(false) }

    val param = when (status) {
        is OfflineAreaItemStatus.NotDownloaded -> AreaPhotosDownloadItemParam(
            text = stringResource(id = R.string.area_overview_download_photos),
            iconRes = R.drawable.ic_download_for_offline,
            onClick = { offlineAreaDownloader.onDownloadArea(areaId) }
        )
        is OfflineAreaItemStatus.Downloading -> AreaPhotosDownloadItemParam(
            text = stringResource(id = R.string.area_overview_cancel_download),
            iconRes = R.drawable.ic_cancel,
            onClick = { offlineAreaDownloader.onCancelAreaDownload(areaId) }
        )
        is OfflineAreaItemStatus.Downloaded -> AreaPhotosDownloadItemParam(
            text = stringResource(id = R.string.area_overview_delete_photos),
            iconRes = R.drawable.ic_delete_forever,
            onClick = { showDeletionDialog = true }
        )
    }

    Column(
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = status !is OfflineAreaItemStatus.NotDownloaded
        ) {
            DownloadInfo(status = status)
        }

        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = param.onClick
        ) {
            Icon(
                painter = painterResource(id = param.iconRes),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = param.text)
        }
    }

    if (showDeletionDialog) {
        AlertDialog(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_error_outline),
                    contentDescription = null,
                    tint = Color.BoolderOrange
                )
            },
            title = { Text(stringResource(id = R.string.photos_download_deletion_dialog_title)) },
            text = { Text(stringResource(id = R.string.photos_download_deletion_dialog_text)) },
            onDismissRequest = { showDeletionDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeletionDialog = false
                        offlineAreaDownloader.onDeleteAreaPhotos(areaId)
                    },
                    content = { Text(stringResource(id = R.string.photos_download_deletion_dialog_confirm_button)) }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeletionDialog = false },
                    content = { Text(stringResource(id = R.string.photos_download_deletion_dialog_cancel_button)) }
                )
            }
        )
    }
}

private data class AreaPhotosDownloadItemParam(
    val text: String,
    @DrawableRes val iconRes: Int,
    val onClick: () -> Unit
)

@Composable
private fun DownloadInfo(status: OfflineAreaItemStatus) {
    when (status) {
        is OfflineAreaItemStatus.NotDownloaded -> Unit
        is OfflineAreaItemStatus.Downloading -> DownloadInfoDownloading(status.progress)
        is OfflineAreaItemStatus.Downloaded -> DownloadInfoDownloaded(status.folderSize)
    }
}

@Composable
private fun DownloadInfoDownloading(progress: Float) {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer { rotationZ = rotationAngle },
                progress = { progress },
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )

            Text(
                text = String.format(Locale.getDefault(), "%.0f%%", progress * 100),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = stringResource(id = R.string.area_overview_download_in_progress),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DownloadInfoDownloaded(folderSize: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = R.drawable.ic_download_done),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(id = R.string.area_overview_downloaded_photos, folderSize),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@PreviewLightDark
@Composable
private fun AreaPhotosDownloadItemPreview(
    @PreviewParameter(AreaPhotosDownloadItemPreviewParameterProvider::class)
    status: OfflineAreaItemStatus
) {
    BoolderTheme {
        AreaPhotosDownloadItem(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface),
            areaId = 42,
            status = status,
            offlineAreaDownloader = dummyOfflineAreaDownloader()
        )
    }
}

private class AreaPhotosDownloadItemPreviewParameterProvider : PreviewParameterProvider<OfflineAreaItemStatus> {
    override val values = sequenceOf(
        OfflineAreaItemStatus.NotDownloaded,
        OfflineAreaItemStatus.Downloading(progress = .4f, progressDetail = "4/10"),
        OfflineAreaItemStatus.Downloaded(folderSize = "33 MB")
    )
}
