package com.boolder.boolder.view.map.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.boolder.boolder.R
import com.boolder.boolder.offline.WORK_DATA_PROGRESS
import com.boolder.boolder.offline.getDownloadTopoImagesWorkName
import com.boolder.boolder.utils.previewgenerator.dummyOfflineAreaItem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus
import kotlin.math.roundToInt

@Composable
internal fun AreaName(
    offlineAreaItem: OfflineAreaItem?,
    onHideAreaName: () -> Unit,
    onAreaInfoClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (offlineAreaItem == null) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha animation"
    )

    Row(
        modifier = modifier
            .alpha(alpha)
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .then(if (alpha == 1f) Modifier.clickable(onClick = onAreaInfoClicked) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.iconActionModifier(onClick = onHideAreaName),
            painter = painterResource(id = R.drawable.ic_chevron_left),
            contentDescription = "Hide area name",
            tint = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            horizontalArrangement = spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = offlineAreaItem?.area?.name.orEmpty(),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            AreaOfflineStatusInfo(item = offlineAreaItem)
        }

        Icon(
            modifier = Modifier.iconActionModifier(onClick = onAreaInfoClicked),
            painter = painterResource(id = R.drawable.ic_outline_info),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

private fun Modifier.iconActionModifier(onClick: () -> Unit) =
    this.padding(4.dp)
        .clip(CircleShape)
        .clickable(onClick = onClick)
        .padding(8.dp)
        .size(24.dp)

@Composable
private fun AreaOfflineStatusInfo(item: OfflineAreaItem?) {
    item ?: return

    when (val status = item.status) {
        is OfflineAreaItemStatus.NotDownloaded -> Unit
        is OfflineAreaItemStatus.Downloading -> AreaDownloadProgress(areaId = status.areaId)
        is OfflineAreaItemStatus.Downloaded -> IconDownloaded()
    }
}

@Composable
private fun AreaDownloadProgress(areaId: Int) {
    if (LocalInspectionMode.current) {
        AreaDownloadProgress(progress = .34f)
        return
    }

    val workInfoList by WorkManager.getInstance(LocalContext.current)
        .getWorkInfosForUniqueWorkLiveData(areaId.getDownloadTopoImagesWorkName())
        .observeAsState()

    val workInfo = workInfoList
        ?.firstOrNull { it.state == WorkInfo.State.RUNNING }
        ?: return

    val progress = workInfo.progress
        .getFloat(WORK_DATA_PROGRESS, 0f)

    AreaDownloadProgress(progress = progress)
}

@Composable
private fun AreaDownloadProgress(progress: Float) {
    Row(
        horizontalArrangement = spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${(progress * 100f).roundToInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun IconDownloaded(
    modifier: Modifier = Modifier
) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_download_done),
        contentDescription = null,
        tint = Color.Gray
    )
}

@PreviewLightDark
@Composable
private fun AreaNamePreview(
    @PreviewParameter(AreaNamePreviewParameterProvider::class)
    offlineAreaItemStatus: OfflineAreaItemStatus
) {
    BoolderTheme {
        AreaName(
            offlineAreaItem = dummyOfflineAreaItem(status = offlineAreaItemStatus),
            onHideAreaName = {},
            onAreaInfoClicked = {}
        )
    }
}

class AreaNamePreviewParameterProvider : PreviewParameterProvider<OfflineAreaItemStatus> {
    override val values = sequenceOf(
        OfflineAreaItemStatus.NotDownloaded,
        OfflineAreaItemStatus.Downloading(areaId = 0),
        OfflineAreaItemStatus.Downloaded(folderSize = "50 MB")
    )
}
