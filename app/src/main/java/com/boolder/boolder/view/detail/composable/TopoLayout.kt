package com.boolder.boolder.view.detail.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.data.userdatabase.entity.TickStatus
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.CircuitInfo
import com.boolder.boolder.domain.model.ProblemWithLine
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.TopoOrigin
import com.boolder.boolder.utils.previewgenerator.dummyCompleteProblem
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun TopoLayout(
    topo: Topo?,
    onProblemPhotoLoaded: () -> Unit,
    onProblemStartClicked: (Int) -> Unit,
    onShowPhotoFullScreen: (Int, String) -> Unit,
    onVariantSelected: (ProblemWithLine) -> Unit,
    onCircuitProblemSelected: (Int) -> Unit,
    onBleauInfoClicked: (String?) -> Unit,
    onShareClicked: (Int) -> Unit,
    onSaveProblem: (Int, TickStatus) -> Unit,
    onUnsaveProblem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        topo?.circuitInfo?.let { circuitInfo ->
            CircuitControls(
                circuitInfo = circuitInfo,
                onPreviousProblemClicked = {
                    circuitInfo.previousProblemId?.let(onCircuitProblemSelected)
                },
                onNextProblemClicked = {
                    circuitInfo.nextProblemId?.let(onCircuitProblemSelected)
                }
            )
        }

        Column(
            modifier = Modifier.clickable(
                onClick = {},
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
        ) {
            TopoPhoto(
                topo = topo,
                onProblemPhotoLoaded = onProblemPhotoLoaded,
                onProblemStartClicked = onProblemStartClicked,
                onShowPhotoFullScreen = onShowPhotoFullScreen,
                onVariantSelected = onVariantSelected
            )

            topo?.selectedCompleteProblem?.let {
                TopoFooter(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding(),
                    problem = it.problemWithLine.problem,
                    onBleauInfoClicked = onBleauInfoClicked,
                    onShareClicked = onShareClicked,
                    onSaveProblem = onSaveProblem,
                    onUnsaveProblem = onUnsaveProblem
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(color = MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TopoLayoutPreview() {
    BoolderTheme {
        TopoLayout(
            topo = Topo(
                photoUri = "uri",
                selectedCompleteProblem = dummyCompleteProblem(),
                otherCompleteProblems = emptyList(),
                circuitInfo = CircuitInfo(
                    color = CircuitColor.RED,
                    previousProblemId = 120,
                    nextProblemId = 123
                ),
                origin = TopoOrigin.MAP,
                canShowProblemStarts = true
            ),
            onProblemPhotoLoaded = {},
            onProblemStartClicked = {},
            onShowPhotoFullScreen = { _, _ -> },
            onVariantSelected = {},
            onCircuitProblemSelected = {},
            onBleauInfoClicked = {},
            onShareClicked = {},
            onSaveProblem = { _, _ -> },
            onUnsaveProblem = {}
        )
    }
}
