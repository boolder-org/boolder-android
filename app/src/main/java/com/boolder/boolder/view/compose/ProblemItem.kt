package com.boolder.boolder.view.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.previewgenerator.dummyProblem

@Composable
internal fun ProblemItem(
    problem: Problem,
    modifier: Modifier = Modifier,
    showFeatured: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProblemIcon(problem = problem)

        Text(
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            text = problem.name.orEmpty()
        )

        if (showFeatured && problem.featured) {
            Icon(
                painter = painterResource(id = R.drawable.ic_favorite),
                contentDescription = null,
                tint = Color.Red
            )
        }

        Text(
            text = problem.grade.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun ProblemIcon(problem: Problem) {
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
            .border(width = 1.dp, color = Color.LightGray, shape = CircleShape),
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

@Preview
@Composable
private fun ProblemItemPreview(
    @PreviewParameter(ProblemItemPreviewParameterProvider::class)
    problem: Problem
) {
    BoolderTheme {
        ProblemItem(
            modifier = Modifier
                .background(color = Color.White)
                .padding(16.dp),
            problem = problem,
            showFeatured = true
        )
    }
}

private class ProblemItemPreviewParameterProvider : PreviewParameterProvider<Problem> {
    override val values = sequenceOf(
        dummyProblem(),
        dummyProblem(featured = true),
        dummyProblem(
            circuitNumber = null,
            circuitColor = null
        ),
        dummyProblem(
            circuitNumber = null,
            circuitColor = null,
            featured = true
        )
    )
}
