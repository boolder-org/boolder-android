package com.boolder.boolder.view.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.previewgenerator.dummyProblem

@Composable
fun ProblemIcon(problem: Problem) {
    val circuitNumber = problem.circuitNumber
    val circuitColor = problem.circuitColorSafe

    Box(
        modifier = Modifier
            .padding(if (circuitNumber == null) 4.dp else 0.dp)
            .size(if (circuitNumber == null) 20.dp else 28.dp)
            .background(
                color = colorResource(id = circuitColor.colorRes),
                shape = CircleShape
            )
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = circuitNumber.orEmpty(),
            color = when (circuitColor) {
                CircuitColor.WHITE,
                CircuitColor.WHITEFORKIDS,
                CircuitColor.OFF_CIRCUIT -> Color.Black

                else -> Color.White
            }
        )
    }
}

@PreviewLightDark
@Composable
private fun ProblemIconPreview(
    @PreviewParameter(ProblemIconPreviewParameterProvider::class)
    problem: Problem
) {
    BoolderTheme {
        ProblemIcon(problem = problem)
    }
}

private class ProblemIconPreviewParameterProvider : PreviewParameterProvider<Problem> {
    override val values = sequenceOf(
        dummyProblem(),
        dummyProblem(
            circuitNumber = null,
            circuitColor = null
        )
    )
}
