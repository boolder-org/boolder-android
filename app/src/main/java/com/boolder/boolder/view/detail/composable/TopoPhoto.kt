package com.boolder.boolder.view.detail.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.TopoOrigin
import com.boolder.boolder.domain.model.toUiProblem
import com.boolder.boolder.utils.previewgenerator.dummyCompleteProblem
import com.boolder.boolder.view.compose.BoolderTheme
import kotlin.math.roundToInt

@Composable
internal fun TopoPhoto(
    topo: Topo?,
    onProblemPhotoLoaded: () -> Unit,
    onProblemStartClicked: (Int) -> Unit,
    onShowPhotoFullScreen: (Int, String) -> Unit,
    onVariantSelected: (ProblemWithLine) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerWidth = with(LocalDensity.current) {
        (LocalConfiguration.current.screenWidthDp * density).roundToInt()
    }
    val containerHeight = (containerWidth * 3f / 4f).roundToInt()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        topo ?: return@Box

        TopoPhotoContent(
            imageUri = topo.photoUri,
            onProblemPhotoLoaded = onProblemPhotoLoaded,
            onShowPhotoFullScreen = {
                val problem = topo.selectedCompleteProblem ?: return@TopoPhotoContent
                val photoUri = topo.photoUri ?: return@TopoPhotoContent

                onShowPhotoFullScreen(problem.problemWithLine.problem.id , photoUri)
            }
        )

        if (topo.canShowProblemStarts) {
            val selectedProblem = topo.selectedCompleteProblem ?: return

            val initialUiProblem = selectedProblem.toUiProblem(
                containerWidthPx = containerWidth,
                containerHeightPx = containerHeight
            )

            val otherUiProblems = topo.otherCompleteProblems.map {
                it.toUiProblem(
                    containerWidthPx = containerWidth,
                    containerHeightPx = containerHeight
                )
            }

            val uiProblems = otherUiProblems + initialUiProblem

            ProblemStartsLayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
                uiProblems = uiProblems,
                selectedProblem = selectedProblem,
                onProblemStartClicked = onProblemStartClicked,
                onVariantSelected = onVariantSelected
            )
        }
    }
}

@Composable
private fun TopoPhotoContent(
    imageUri: String?,
    onProblemPhotoLoaded: () -> Unit,
    onShowPhotoFullScreen: () -> Unit
) {
    if (LocalInspectionMode.current) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.area_cover_1),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                color = Color.White.copy(alpha = .5f),
                blendMode = BlendMode.Lighten
            )
        )
    } else {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            onState = { if (it is AsyncImagePainter.State.Success) onProblemPhotoLoaded() }
        ) {
            val state = painter.state

            when (state) {
                is AsyncImagePainter.State.Empty -> Unit
                is AsyncImagePainter.State.Loading -> CircularProgressIndicator()
                is AsyncImagePainter.State.Success -> Image(
                    modifier = Modifier.clickable(
                        onClick = onShowPhotoFullScreen,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ),
                    painter = painter,
                    contentDescription = null
                )
                is AsyncImagePainter.State.Error -> Icon(
                    painter = painterResource(id = R.drawable.ic_placeholder),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TopoPhotoPreview(
    @PreviewParameter(TopoPhotoPreviewParameterProvider::class)
    topo: Topo
) {
    BoolderTheme {
        TopoPhoto(
            topo = topo,
            onProblemPhotoLoaded = {},
            onProblemStartClicked = {},
            onShowPhotoFullScreen = { _, _ -> },
            onVariantSelected = {}
        )
    }
}

private class TopoPhotoPreviewParameterProvider : PreviewParameterProvider<Topo?> {
    private val topo = Topo(
        photoUri = "uri",
        selectedCompleteProblem = dummyCompleteProblem(),
        otherCompleteProblems = emptyList(),
        circuitInfo = null,
        origin = TopoOrigin.MAP
    )

    override val values = sequenceOf(
        null,
        topo,
        topo.copy(canShowProblemStarts = true)
    )
}
