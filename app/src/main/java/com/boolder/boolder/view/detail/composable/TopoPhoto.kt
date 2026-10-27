package com.boolder.boolder.view.detail.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.boolder.boolder.domain.model.BoulderTopo
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
    onTopoSwiped: (Int) -> Unit,
    onShowPhotoFullScreen: (Int, String) -> Unit,
    onPremiumRequested: () -> Unit,
    onVariantSelected: (ProblemWithLine) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        topo ?: return@Box

        if (topo.boulderTopos.size > 1) {
            TopoPhotoPager(
                topo = topo,
                onProblemPhotoLoaded = onProblemPhotoLoaded,
                onProblemStartClicked = onProblemStartClicked,
                onTopoSwiped = onTopoSwiped,
                onShowPhotoFullScreen = onShowPhotoFullScreen,
                onPremiumRequested = onPremiumRequested,
                onVariantSelected = onVariantSelected
            )
        } else {
            TopoPhotoPage(
                topo = topo,
                photoUri = topo.photoUri,
                isSelectedTopo = true,
                onProblemPhotoLoaded = onProblemPhotoLoaded,
                onProblemStartClicked = onProblemStartClicked,
                onShowPhotoFullScreen = onShowPhotoFullScreen,
                onPremiumRequested = onPremiumRequested,
                onVariantSelected = onVariantSelected
            )
        }
    }
}

@Composable
private fun TopoPhotoPager(
    topo: Topo,
    onProblemPhotoLoaded: () -> Unit,
    onProblemStartClicked: (Int) -> Unit,
    onTopoSwiped: (Int) -> Unit,
    onShowPhotoFullScreen: (Int, String) -> Unit,
    onPremiumRequested: () -> Unit,
    onVariantSelected: (ProblemWithLine) -> Unit
) {
    val boulderTopos = topo.boulderTopos

    // a new boulder starts a new pager, a swipe on the same boulder keeps it
    key(boulderTopos.map { it.topoId }) {
        val pagerState = rememberPagerState(
            initialPage = boulderTopos.indexOfFirst { it.topoId == topo.topoId }.coerceAtLeast(0),
            pageCount = { boulderTopos.size }
        )
        val selectedTopoId by rememberUpdatedState(topo.topoId)

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                val topoId = boulderTopos[page].topoId

                if (topoId != selectedTopoId) onTopoSwiped(topoId)
            }
        }

        // a problem selected elsewhere (map, search, circuit) can be on another page
        LaunchedEffect(topo.topoId) {
            val selectedPage = boulderTopos.indexOfFirst { it.topoId == topo.topoId }

            if (selectedPage < 0 || selectedPage == pagerState.currentPage) return@LaunchedEffect
            if (pagerState.isScrollInProgress) return@LaunchedEffect

            pagerState.animateScrollToPage(selectedPage)
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            key = { boulderTopos[it].topoId }
        ) { page ->
            val boulderTopo = boulderTopos[page]

            TopoPhotoPage(
                topo = topo,
                photoUri = boulderTopo.photoUri,
                isSelectedTopo = boulderTopo.topoId == topo.topoId,
                onProblemPhotoLoaded = onProblemPhotoLoaded,
                onProblemStartClicked = onProblemStartClicked,
                onShowPhotoFullScreen = onShowPhotoFullScreen,
                onPremiumRequested = onPremiumRequested,
                onVariantSelected = onVariantSelected
            )
        }
    }
}

@Composable
private fun TopoPhotoPage(
    topo: Topo,
    photoUri: String?,
    isSelectedTopo: Boolean,
    onProblemPhotoLoaded: () -> Unit,
    onProblemStartClicked: (Int) -> Unit,
    onShowPhotoFullScreen: (Int, String) -> Unit,
    onPremiumRequested: () -> Unit,
    onVariantSelected: (ProblemWithLine) -> Unit
) {
    val containerWidth = with(LocalDensity.current) {
        (LocalConfiguration.current.screenWidthDp * density).roundToInt()
    }
    val containerHeight = (containerWidth * 3f / 4f).roundToInt()

    // a page can be loaded before it gets selected: report it once it is both
    var isPhotoLoaded by remember(photoUri) { mutableStateOf(false) }

    LaunchedEffect(isSelectedTopo, isPhotoLoaded) {
        if (isSelectedTopo && isPhotoLoaded) onProblemPhotoLoaded()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TopoPhotoContent(
            imageUri = photoUri,
            isPhotoLocked = topo.isPhotoLocked,
            premiumPrice = topo.premiumPrice,
            onProblemPhotoLoaded = { isPhotoLoaded = true },
            onShowPhotoFullScreen = {
                if (!isSelectedTopo) return@TopoPhotoContent

                val problem = topo.selectedCompleteProblem ?: return@TopoPhotoContent
                val topoPhotoUri = topo.photoUri ?: return@TopoPhotoContent

                onShowPhotoFullScreen(problem.problemWithLine.problem.id, topoPhotoUri)
            },
            onPremiumRequested = onPremiumRequested
        )

        if (isSelectedTopo && topo.canShowProblemStarts && !topo.isPhotoLocked) {
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
    isPhotoLocked: Boolean,
    premiumPrice: String?,
    onProblemPhotoLoaded: () -> Unit,
    onShowPhotoFullScreen: () -> Unit,
    onPremiumRequested: () -> Unit
) {
    if (LocalInspectionMode.current && isPhotoLocked) {
        LockedPhoto(
            painter = painterResource(id = R.drawable.area_cover_1),
            premiumPrice = premiumPrice,
            onPremiumRequested = onPremiumRequested
        )
    } else if (LocalInspectionMode.current) {
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
                is AsyncImagePainter.State.Success -> if (isPhotoLocked) {
                    LockedPhoto(
                        painter = painter,
                        premiumPrice = premiumPrice,
                        onPremiumRequested = onPremiumRequested
                    )
                } else {
                    Image(
                        modifier = Modifier.clickable(
                            onClick = onShowPhotoFullScreen,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ),
                        painter = painter,
                        contentDescription = null
                    )
                }
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
            onTopoSwiped = {},
            onShowPhotoFullScreen = { _, _ -> },
            onPremiumRequested = {},
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
        topo.copy(canShowProblemStarts = true),
        topo.copy(
            canShowProblemStarts = true,
            topoId = 1,
            boulderTopos = listOf(
                BoulderTopo(topoId = 1, photoUri = "uri"),
                BoulderTopo(topoId = 2, photoUri = "uri")
            )
        ),
        topo.copy(canShowProblemStarts = true, isPhotoLocked = true, premiumPrice = "10,00 €"),
        topo.copy(canShowProblemStarts = true, isPhotoLocked = true)
    )
}
