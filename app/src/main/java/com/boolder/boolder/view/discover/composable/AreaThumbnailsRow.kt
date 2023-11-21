package com.boolder.boolder.view.discover.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.utils.previewgenerator.dummyArea
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun AreaThumbnailsRow(
    areas: List<Area>,
    onAreaClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val itemWidth = (configuration.screenWidthDp.dp - 16.dp * 2 - 8.dp) / 2

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Absolute.spacedBy(8.dp)
    ) {
        items(areas) { area ->
            AreaThumbnailItem(
                modifier = Modifier.width(itemWidth),
                area = area,
                onClick = { onAreaClicked(area.id) }
            )
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
private fun AreaThumbnailItem(
    area: Area,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawableRes = if (!LocalInspectionMode.current) {
        val context = LocalContext.current

        context.resources.getIdentifier(
            "area_cover_${area.id}",
            "drawable",
            context.packageName
        ).takeIf { it != 0 } ?: return
    } else {
        R.drawable.area_cover_1
    }

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = .2f),
                            Color.Black.copy(alpha = .1f)
                        )
                    )
                )
        )

        Text(
            modifier = Modifier.padding(4.dp),
            text = area.name.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
private fun AreaThumbnailsRowPreview() {
    BoolderTheme {
        AreaThumbnailsRow(
            modifier = Modifier
                .background(color = Color.White)
                .padding(vertical = 8.dp),
            areas = List(10) {
                dummyArea(
                    id = it,
                    name = "Area $it",
                    problemsCount = it * 100
                )
            },
            onAreaClicked = {}
        )
    }
}
