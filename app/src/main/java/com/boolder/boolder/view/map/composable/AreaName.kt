package com.boolder.boolder.view.map.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boolder.boolder.R
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.utils.previewgenerator.dummyOfflineAreaItem
import com.boolder.boolder.view.compose.BoolderOrange
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem

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
        }

        Box {
            Icon(
                modifier = Modifier.iconActionModifier(onClick = onAreaInfoClicked),
                painter = painterResource(id = R.drawable.ic_outline_info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            if (offlineAreaItem?.area?.warning != null) {
                Icon(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(16.dp)
                        .align(Alignment.TopEnd),
                    painter = painterResource(id = R.drawable.ic_round_warning),
                    contentDescription = null,
                    tint = Color.BoolderOrange
                )
            }
        }
    }
}

private fun Modifier.iconActionModifier(onClick: () -> Unit) =
    this.padding(4.dp)
        .clip(CircleShape)
        .clickable(onClick = onClick)
        .padding(8.dp)
        .size(24.dp)

@PreviewLightDark
@Composable
private fun AreaNamePreview(
    @PreviewParameter(AreaNamePreviewParameterProvider::class)
    offlineAreaItem: OfflineAreaItem
) {
    BoolderTheme {
        AreaName(
            offlineAreaItem = offlineAreaItem,
            onHideAreaName = {},
            onAreaInfoClicked = {}
        )
    }
}

class AreaNamePreviewParameterProvider : PreviewParameterProvider<OfflineAreaItem> {

    override val values = sequenceOf(
        dummyOfflineAreaItem(area = dummyArea(warning = null)),
        dummyOfflineAreaItem()
    )
}
