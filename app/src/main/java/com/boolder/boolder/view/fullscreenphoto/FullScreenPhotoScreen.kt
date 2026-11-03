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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import coil.compose.rememberAsyncImagePainter
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.domain.model.allProblems
import com.boolder.boolder.domain.model.toUiProblem
import com.boolder.boolder.utils.previewgenerator.dummyCompleteProblem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.LoadingScreen
import com.boolder.boolder.view.detail.composable.LockedPhoto
import com.boolder.boolder.view.detail.composable.ProblemStartsLayer
import com.boolder.boolder.view.detail.composable.TopoThumbnailStrip
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
internal fun FullScreenPhotoScreen(
    screenState: FullScreenPhotoViewModel.ScreenState,
    scrollOffset: Float,
    onCloseClicked: () -> Unit,
    onDragStateChanged: (Boolean) -> Unit,
    onTopoSwiped: (Int) -> Unit,
    onProblemSelected: (Int) -> Unit,
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
                    onDragStateChanged = onDragStateChanged,
                    onTopoSwiped = onTopoSwiped,
                    onProblemSelected = onProblemSelected
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

@Composable
private fun FullScreenPhotoScreenContent(
    screenState: FullScreenPhotoViewModel.ScreenState.Content,
    onDragStateChanged: (Boolean) -> Unit,
    onTopoSwiped: (Int) -> Unit,
    onProblemSelected: (Int) -> Unit
) {
    val boulderTopos = screenState.boulderTopos

    if (boulderTopos.size <= 1) {
        FullScreenPhotoPage(
            photoUri = screenState.photoUri,
            selectedProblem = screenState.completeProblem,
            otherProblems = emptyList(),
            showAllLines = false,
            isPhotoLocked = screenState.isPhotoLocked,
            onProblemSelected = {},
            onZoomStateChanged = { isZoomed -> onDragStateChanged(!isZoomed) }
        )

        return
    }

    val shownTopo = screenState.shownTopo
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = boulderTopos.indexOfFirst { it.topoId == screenState.topoId }.coerceAtLeast(0),
        pageCount = { boulderTopos.size }
    )
    var isZoomed by remember { mutableStateOf(false) }

    LaunchedEffect(isZoomed) {
        onDragStateChanged(!isZoomed)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .drop(1)
            .collect { onTopoSwiped(boulderTopos[it].topoId) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // a zoomed photo is panned instead of changing the page
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            userScrollEnabled = !isZoomed,
            beyondViewportPageCount = 1,
            key = { boulderTopos[it].topoId }
        ) { page ->
            val boulderTopo = boulderTopos[page]
            val pageTopo = shownTopo?.takeIf { it.topoId == boulderTopo.topoId }

            FullScreenPhotoPage(
                photoUri = boulderTopo.photoUri,
                selectedProblem = when {
                    pageTopo != null -> pageTopo.selectedCompleteProblem
                    shownTopo == null && boulderTopo.topoId == screenState.topoId -> screenState.completeProblem
                    else -> null
                },
                otherProblems = pageTopo?.otherCompleteProblems.orEmpty(),
                showAllLines = pageTopo?.isTopoMode == true,
                isPhotoLocked = screenState.isPhotoLocked,
                onProblemSelected = onProblemSelected,
                onZoomStateChanged = { if (page == pagerState.currentPage) isZoomed = it }
            )
        }

        if (shownTopo?.isTopoMode == true) {
            TopoThumbnailStrip(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(color = MaterialTheme.colorScheme.background.copy(alpha = .9f)),
                boulderTopos = boulderTopos,
                selectedTopoId = shownTopo.topoId,
                isPhotoLocked = screenState.isPhotoLocked,
                problems = shownTopo.allProblems().sortedBy { it.grade },
                onTopoSelected = { topoId ->
                    val page = boulderTopos.indexOfFirst { it.topoId == topoId }

                    coroutineScope.launch { pagerState.animateScrollToPage(page) }
                },
                onProblemSelected = onProblemSelected
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FullScreenPhotoPage(
    photoUri: String,
    selectedProblem: CompleteProblem?,
    otherProblems: List<CompleteProblem>,
    showAllLines: Boolean,
    isPhotoLocked: Boolean,
    onProblemSelected: (Int) -> Unit,
    onZoomStateChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isPhotoLocked) {
            LockedPhoto(
                painter = if (LocalInspectionMode.current) {
                    painterResource(id = R.drawable.area_cover_1)
                } else {
                    rememberAsyncImagePainter(model = photoUri)
                },
                premiumPrice = null,
                onPremiumRequested = null,
                imageModifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4 / 3f)
            )

            return@Box
        }

        val widthPx = with(LocalDensity.current) {
            (LocalConfiguration.current.screenWidthDp * density).roundToInt()
        }
        val heightPx = (widthPx * 3f / 4f).roundToInt()

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

        val isZoomed by remember { derivedStateOf { scaleFactor != 1f } }

        LaunchedEffect(key1 = isZoomed) {
            onZoomStateChanged(isZoomed)
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
                    },
                    // a single finger swipes the pages until the photo is zoomed
                    canPan = { targetScaleFactor > 1f }
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
            model = photoUri,
            placeholder = if (LocalInspectionMode.current) painterResource(id = R.drawable.area_cover_1) else null,
            contentDescription = null,
            contentScale = ContentScale.Fit
        )

        selectedProblem ?: return@Box

        val uiProblems = (otherProblems + selectedProblem).map {
            it.toUiProblem(
                containerWidthPx = widthPx,
                containerHeightPx = heightPx
            )
        }

        ProblemStartsLayer(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4 / 3f)
                .offset { IntOffset(x = offset.x.roundToInt(), y = offset.y.roundToInt()) }
                .graphicsLayer {
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                },
            uiProblems = uiProblems,
            selectedProblem = selectedProblem,
            showAllLines = showAllLines,
            drawnElementsScaleFactor = 1f / scaleFactor,
            onProblemStartClicked = onProblemSelected,
            onVariantSelected = { onProblemSelected(it.problem.id) }
        )
    }
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
            onDragStateChanged = {},
            onTopoSwiped = {},
            onProblemSelected = {}
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
        FullScreenPhotoViewModel.ScreenState.Content(
            photoUri = "photoUri",
            completeProblem = dummyCompleteProblem(),
            isPhotoLocked = true
        ),
        FullScreenPhotoViewModel.ScreenState.Error
    )
}
