package com.boolder.boolder.view.detail.composable

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.BoulderTopo
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.previewgenerator.dummyProblem
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun TopoThumbnailStrip(
    boulderTopos: List<BoulderTopo>,
    selectedTopoId: Int?,
    isPhotoLocked: Boolean,
    problems: List<Problem>,
    onTopoSelected: (Int) -> Unit,
    onProblemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isProblemsListShown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // all the thumbnails fit in the width: they get narrower on boulders with many pictures
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val spacing = 8.dp
            val thumbnailCount = boulderTopos.size.coerceAtLeast(1)
            val thumbnailWidth = ((maxWidth - spacing * (thumbnailCount - 1)) / thumbnailCount)
                .coerceIn(0.dp, 72.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
            ) {
                boulderTopos.forEach { boulderTopo ->
                    val isSelected = boulderTopo.topoId == selectedTopoId

                    TopoThumbnail(
                        photoUri = boulderTopo.photoUri,
                        width = thumbnailWidth,
                        isSelected = isSelected,
                        isPhotoLocked = isPhotoLocked,
                        onClick = { if (!isSelected) onTopoSelected(boulderTopo.topoId) }
                    )
                }
            }
        }

        TextButton(onClick = { isProblemsListShown = true }) {
            Text(
                text = pluralStringResource(id = R.plurals.topo_problems, count = problems.size, problems.size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (isProblemsListShown) {
        TopoProblemsBottomSheet(
            problems = problems,
            onProblemSelected = {
                isProblemsListShown = false
                onProblemSelected(it)
            },
            onDismissRequest = { isProblemsListShown = false }
        )
    }
}

@Composable
private fun TopoThumbnail(
    photoUri: String,
    width: Dp,
    isSelected: Boolean,
    isPhotoLocked: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)

    val imageModifier = Modifier
        .size(width = width, height = 54.dp)
        .clip(shape)
        .border(
            width = 3.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = shape
        )
        .clickable(onClick = onClick)
        .then(if (isPhotoLocked) Modifier.lockedThumbnail() else Modifier)

    if (LocalInspectionMode.current) {
        Image(
            modifier = imageModifier,
            painter = painterResource(id = R.drawable.area_cover_1),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            modifier = imageModifier,
            model = photoUri,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}

// Modifier.blur has no effect before Android 12, the photo is hidden instead
private fun Modifier.lockedThumbnail(): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        blur(radius = 4.dp)
    } else {
        drawWithContent { drawRect(color = Color.Black) }
    }

@PreviewLightDark
@Composable
private fun TopoThumbnailStripPreview() {
    BoolderTheme {
        TopoThumbnailStrip(
            boulderTopos = (1..5).map { BoulderTopo(topoId = it, photoUri = "uri") },
            selectedTopoId = 3,
            isPhotoLocked = false,
            problems = listOf(dummyProblem()),
            onTopoSelected = {},
            onProblemSelected = {}
        )
    }
}
