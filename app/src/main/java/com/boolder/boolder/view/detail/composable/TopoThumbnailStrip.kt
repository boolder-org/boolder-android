package com.boolder.boolder.view.detail.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.BoulderTopo
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun TopoThumbnailStrip(
    boulderTopos: List<BoulderTopo>,
    selectedTopoId: Int?,
    isPhotoLocked: Boolean,
    onTopoSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // keep the selected thumbnail visible when the photo is swiped
    LaunchedEffect(selectedTopoId) {
        val index = boulderTopos.indexOfFirst { it.topoId == selectedTopoId }

        if (index < 0) return@LaunchedEffect
        if (listState.layoutInfo.visibleItemsInfo.any { it.index == index }) return@LaunchedEffect

        listState.animateScrollToItem(index)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(vertical = 16.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            items(boulderTopos, key = { it.topoId }) { boulderTopo ->
                val isSelected = boulderTopo.topoId == selectedTopoId

                TopoThumbnail(
                    photoUri = boulderTopo.photoUri,
                    isSelected = isSelected,
                    isPhotoLocked = isPhotoLocked,
                    onClick = { if (!isSelected) onTopoSelected(boulderTopo.topoId) }
                )
            }
        }
    }
}

@Composable
private fun TopoThumbnail(
    photoUri: String,
    isSelected: Boolean,
    isPhotoLocked: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)

    val imageModifier = Modifier
        .size(width = 72.dp, height = 54.dp)
        .clip(shape)
        .border(
            width = 3.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = shape
        )
        .clickable(onClick = onClick)
        .then(if (isPhotoLocked) Modifier.blur(radius = 4.dp) else Modifier)

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

@PreviewLightDark
@Composable
private fun TopoThumbnailStripPreview() {
    BoolderTheme {
        TopoThumbnailStrip(
            boulderTopos = (1..5).map { BoulderTopo(topoId = it, photoUri = "uri") },
            selectedTopoId = 3,
            isPhotoLocked = false,
            onTopoSelected = {}
        )
    }
}
