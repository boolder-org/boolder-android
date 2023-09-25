package com.boolder.boolder.view.detail.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.CompleteProblem
import com.boolder.boolder.utils.extension.composeColor
import com.boolder.boolder.utils.previewgenerator.dummyCompleteProblem
import com.boolder.boolder.utils.previewgenerator.dummyProblemStart
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.detail.uimodel.ProblemStart
import kotlin.math.roundToInt

@Composable
internal fun ProblemStartsLayer(
    problemStarts: List<ProblemStart>,
    selectedProblem: CompleteProblem?,
    onProblemStartClicked: (CompleteProblem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        problemStarts.forEach { problemStart ->
            val shadowRadius = 2.dp

            MarkerShadow(
                problemStart = problemStart,
                shadowRadius = shadowRadius
            )

            if (problemStart.completeProblem.problem.id != selectedProblem?.problem?.id) {
                ProblemStartMarker(
                    problemStart = problemStart,
                    modifier = Modifier.clickable {
                        onProblemStartClicked(problemStart.completeProblem)
                    }
                )
            }
        }

        if (selectedProblem != null) {
            ProblemLine(
                line = selectedProblem.line,
                color = selectedProblem.problem.circuitColorSafe.composeColor()
            )

            problemStarts
                .find { it.completeProblem.problem.id == selectedProblem.problem.id }
                ?.let {
                    ProblemStartMarker(
                        problemStart = it,
                        modifier = Modifier
                    )
                }
        }
    }
}

@Composable
private fun ProblemStartMarker(
    problemStart: ProblemStart,
    modifier: Modifier
) {
    val markerHalfSizePx = with(LocalDensity.current) {
        (problemStart.dpSize.dp / 2).toPx().roundToInt()
    }

    Box(
        modifier = Modifier
            .size(problemStart.dpSize.dp)
            .offset {
                IntOffset(
                    x = problemStart.x - markerHalfSizePx,
                    y = problemStart.y - markerHalfSizePx
                )
            }
            .background(
                color = colorResource(id = problemStart.colorRes),
                shape = CircleShape
            )
            .clip(shape = CircleShape)
            .then(modifier)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = problemStart.completeProblem.problem.circuitNumber.orEmpty(),
            color = colorResource(id = problemStart.textColorRes),
            fontSize = 18.sp
        )
    }
}

@Composable
private fun MarkerShadow(
    problemStart: ProblemStart,
    shadowRadius: Dp
) {
    val markerHalfSizePx = with(LocalDensity.current) {
        (problemStart.dpSize.dp / 2).toPx().roundToInt()
    }

    Box(
        modifier = Modifier
            .size(problemStart.dpSize.dp + shadowRadius * 2)
            .offset {
                val shadowRadiusPx = shadowRadius
                    .toPx()
                    .roundToInt()

                IntOffset(
                    x = problemStart.x - markerHalfSizePx - shadowRadiusPx,
                    y = problemStart.y - markerHalfSizePx - shadowRadiusPx
                )
            }
            .blur(
                radius = shadowRadius,
                edgeTreatment = BlurredEdgeTreatment.Unbounded
            )
            .background(
                color = colorResource(id = R.color.problem_line_shadow),
                shape = CircleShape
            )
    )
}

/**
 * Use the preview interactive mode in order to make the line appearing
 */
@Preview
@Composable
internal fun ProblemStartsLayerPreview() {
    val completeProblem = dummyCompleteProblem()

    BoolderTheme {
        BoxWithConstraints {
            ProblemStartsLayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(color = Color.White),
                problemStarts = listOf(
                    dummyProblemStart(
                        x = (0.76 * constraints.maxWidth).roundToInt(),
                        y = (0.7533 * constraints.maxHeight).roundToInt()
                    )
                ),
                selectedProblem = completeProblem,
                onProblemStartClicked = {}
            )
        }
    }
}
