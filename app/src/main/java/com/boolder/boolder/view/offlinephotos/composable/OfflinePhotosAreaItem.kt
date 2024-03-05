package com.boolder.boolder.view.offlinephotos.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.boolder.boolder.R
import com.boolder.boolder.offline.DOWNLOAD_TERMINATED_STATUSES
import com.boolder.boolder.offline.WORK_DATA_PROGRESS
import com.boolder.boolder.offline.WORK_DATA_PROGRESS_DETAIL
import com.boolder.boolder.offline.getDownloadTopoImagesWorkName
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus

@Composable
fun OfflinePhotosAreaItem(
    areaName: String,
    status: OfflineAreaItemStatus,
    onDownloadClicked: () -> Unit,
    onDownloadTerminated: () -> Unit,
    onCancelDownload: () -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    val internalModifier = modifier
        .fillMaxWidth()
        .height(56.dp)
        .clip(shape)
        .background(color = MaterialTheme.colorScheme.surface)

    when (status) {
        is OfflineAreaItemStatus.NotDownloaded -> OfflinePhotosAreaNotDownloadedItem(
            modifier = internalModifier,
            areaName = areaName,
            onDownloadClicked = onDownloadClicked
        )

        is OfflineAreaItemStatus.Downloading -> OfflinePhotosAreaDownloadingItem(
            modifier = internalModifier,
            areaName = areaName,
            areaId = status.areaId,
            onDownloadTerminated = onDownloadTerminated,
            onCancelDownload = onCancelDownload
        )

        is OfflineAreaItemStatus.Downloaded -> OfflinePhotosAreaDownloadedItem(
            modifier = internalModifier,
            areaName = areaName,
            folderSize = status.folderSize,
            onDeleteClicked = onDeleteClicked
        )
    }
}

@Composable
private fun OfflinePhotosAreaNotDownloadedItem(
    areaName: String,
    onDownloadClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.Absolute.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AreaName(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            areaName = areaName
        )

        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onDownloadClicked)
                .padding(8.dp),
            painter = painterResource(id = R.drawable.ic_download_for_offline),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun OfflinePhotosAreaDownloadingItem(
    areaName: String,
    areaId: Int,
    onDownloadTerminated: () -> Unit,
    onCancelDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        val progressModifier = Modifier
            .fillMaxWidth()
            .height(56.dp)

        if (LocalInspectionMode.current) {
            Box(
                modifier = progressModifier
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = .4f
                    }
                    .background(color = Color.Gray.copy(alpha = .3f))
            )

            OfflinePhotosAreaDownloadingItemContent(
                areaName = areaName,
                progressDetail = "4/10",
                onCancelDownload = {}
            )
        } else {
            val workInfoList by WorkManager.getInstance(LocalContext.current)
                .getWorkInfosForUniqueWorkLiveData(areaId.getDownloadTopoImagesWorkName())
                .observeAsState()

            if (workInfoList?.all { it.state in DOWNLOAD_TERMINATED_STATUSES } == true) {
                onDownloadTerminated()
            }

            val workInfo = workInfoList
                ?.firstOrNull { it.state == WorkInfo.State.RUNNING }
                ?: return@Box

            val progress = workInfo.progress
                .getFloat(WORK_DATA_PROGRESS, 0f)

            Box(
                modifier = progressModifier
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = progress
                    }
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .3f)
                    )
            )

            val progressDetail = workInfo.progress
                .getString(WORK_DATA_PROGRESS_DETAIL)

            OfflinePhotosAreaDownloadingItemContent(
                areaName = areaName,
                progressDetail = progressDetail,
                onCancelDownload = onCancelDownload
            )
        }
    }
}

@Composable
private fun OfflinePhotosAreaDownloadingItemContent(
    areaName: String,
    progressDetail: String?,
    onCancelDownload: () -> Unit
) {
    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.Absolute.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp)
        )

        AreaName(
            modifier = Modifier.weight(1f),
            areaName = areaName
        )

        if (!progressDetail.isNullOrEmpty()) {
            Text(
                text = "($progressDetail)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onCancelDownload)
                .padding(8.dp),
            painter = painterResource(id = R.drawable.ic_cancel),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OfflinePhotosAreaDownloadedItem(
    areaName: String,
    folderSize: String,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.Absolute.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp),
            painter = painterResource(id = R.drawable.ic_download_done),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        AreaName(
            modifier = Modifier.weight(1f),
            areaName = areaName
        )

        Text(
            text = "($folderSize)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onDeleteClicked)
                .padding(8.dp),
            painter = painterResource(id = R.drawable.ic_delete_forever),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AreaName(
    areaName: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = areaName,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@PreviewLightDark
@Composable
private fun OfflinePhotosAreaItemPreview(
    @PreviewParameter(OfflinePhotosAreaItemPreviewParameterProvider::class)
    status: OfflineAreaItemStatus
) {
    BoolderTheme {
        OfflinePhotosAreaItem(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(16.dp),
            areaName = "Apremont",
            status = status,
            onDownloadClicked = {},
            onDownloadTerminated = {},
            onCancelDownload = {},
            onDeleteClicked = {}
        )
    }
}

private class OfflinePhotosAreaItemPreviewParameterProvider :
    PreviewParameterProvider<OfflineAreaItemStatus> {
    override val values = sequenceOf(
        OfflineAreaItemStatus.NotDownloaded,
        OfflineAreaItemStatus.Downloading(areaId = 7),
        OfflineAreaItemStatus.Downloaded(folderSize = "50 MB"),
    )
}
