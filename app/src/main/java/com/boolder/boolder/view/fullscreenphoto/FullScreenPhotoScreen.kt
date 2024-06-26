package com.boolder.boolder.view.fullscreenphoto

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import coil.compose.AsyncImage
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.toUiProblem
import com.boolder.boolder.utils.previewgenerator.dummyCompleteProblem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.LoadingScreen
import com.boolder.boolder.view.detail.composable.ProblemStartsLayer
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
internal fun FullScreenPhotoScreen(
    screenState: FullScreenPhotoViewModel.ScreenState,
    scrollOffset: Float,
    onCloseClicked: () -> Unit,
    onDragStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val radiusSize = (1f - scrollOffset) * 24.dp

    Column(
        modifier = modifier.clip(
            shape = RoundedCornerShape(topStart = radiusSize, topEnd = radiusSize)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topInset * scrollOffset + 8.dp)
                .background(color = MaterialTheme.colorScheme.background)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = MaterialTheme.colorScheme.background)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            when (screenState) {
                is FullScreenPhotoViewModel.ScreenState.Loading -> LoadingScreen(
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
                )

                is FullScreenPhotoViewModel.ScreenState.Content -> FullScreenPhotoScreenContent(
                    screenState = screenState,
                    onDragStateChanged = onDragStateChanged
                )

                is FullScreenPhotoViewModel.ScreenState.Error -> FullScreenPhotoScreenError()
            }

            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp),
                onClick = onCloseClicked
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FullScreenPhotoScreenContent(
    screenState: FullScreenPhotoViewModel.ScreenState.Content,
    onDragStateChanged: (Boolean) -> Unit
) {
    val widthPx = with(LocalDensity.current) {
        (LocalConfiguration.current.screenWidthDp * density).roundToInt()
    }
    val heightPx = (widthPx * 3f / 4f).roundToInt()

    val uiProblem = screenState.completeProblem.toUiProblem(
        containerWidthPx = widthPx,
        containerHeightPx = heightPx
    )

    var targetOffset by remember { mutableStateOf(Offset.Zero) }
    val offset by animateOffsetAsState(
        targetValue = targetOffset,
        label = "photo_offset_animation"
    )

    var targetScaleFactor by remember { mutableFloatStateOf(1f) }
    val scaleFactor by animateFloatAsState(
        targetValue = targetScaleFactor,
        label = "photo_scale_animation"
    )

    val canDragParentContainer by remember { derivedStateOf { scaleFactor == 1f } }

    LaunchedEffect(key1 = canDragParentContainer) {
        onDragStateChanged(canDragParentContainer)
    }

    AsyncImage(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(x = offset.x.roundToInt(), y = offset.y.roundToInt()) }
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .transformable(
                state = rememberTransformableState { zoomChange, panChange, _ ->
                    targetScaleFactor = max(targetScaleFactor * zoomChange, 1f)

                    val rawOffset = targetOffset + (panChange * targetScaleFactor)
                    val scaleDeltaX = ((widthPx * targetScaleFactor) - widthPx) / 2f
                    val scaleDeltaY = ((heightPx * targetScaleFactor) - heightPx) / 2f

                    targetOffset = Offset(
                        x = rawOffset.x.coerceIn(-scaleDeltaX..scaleDeltaX),
                        y = rawOffset.y.coerceIn(-scaleDeltaY..scaleDeltaY)
                    )
                }
            )
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onDoubleClick = {
                    targetScaleFactor = if (targetScaleFactor == 1f) 2.5f else 1f
                    targetOffset = Offset.Zero
                }
            ),
        model = screenState.photoUri,
        placeholder = if (LocalInspectionMode.current) painterResource(id = R.drawable.area_cover_1) else null,
        contentDescription = null,
        contentScale = ContentScale.Fit
    )

    ProblemStartsLayer(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4 / 3f)
            .offset { IntOffset(x = offset.x.roundToInt(), y = offset.y.roundToInt()) }
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            },
        uiProblems = listOf(uiProblem),
        selectedProblem = uiProblem.completeProblem,
        drawnElementsScaleFactor = 1f / scaleFactor,
        onProblemStartClicked = {},
        onVariantSelected = {}
    )
}

@Composable
private fun FullScreenPhotoScreenError() {
    Text(
        modifier = Modifier.padding(24.dp),
        text = stringResource(id = R.string.full_screen_photo_error),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@PreviewLightDark
@Composable
private fun FullScreenPhotoScreenPreview(
    @PreviewParameter(FullScreenPhotoScreenPreviewParameterProvider::class)
    screenState: FullScreenPhotoViewModel.ScreenState
) {
    BoolderTheme {
        FullScreenPhotoScreen(
            screenState = screenState,
            scrollOffset = 1f,
            onCloseClicked = {},
            onDragStateChanged = {}
        )
    }
}

private class FullScreenPhotoScreenPreviewParameterProvider : PreviewParameterProvider<FullScreenPhotoViewModel.ScreenState> {
    override val values = sequenceOf(
        FullScreenPhotoViewModel.ScreenState.Loading,
        FullScreenPhotoViewModel.ScreenState.Content(
            photoUri = "photoUri",
            completeProblem = dummyCompleteProblem()
        ),
        FullScreenPhotoViewModel.ScreenState.Error
    )
}
