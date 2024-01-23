package com.boolder.boolder.view.compose.degreecounts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun DegreeCountsChart(
    degreeCounts: Map<String, Int>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.aspectRatio(16f / 9f),
    ) {
        DegreesChart(
            modifier = modifier.weight(1f),
            degreeCounts = degreeCounts.values,
            color = color
        )

        DegreesLabels(degrees = degreeCounts.keys)
    }
}

@Composable
private fun DegreesChart(
    degreeCounts: Collection<Int>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val maxDegreeCount = degreeCounts.max().coerceAtLeast(150)

    Box(
        modifier = modifier.padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.Bottom
        ) {
            degreeCounts.forEach { count ->
                val heightFraction = count / maxDegreeCount.toFloat()

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = .8f)
                            .weight(weight = 1f, fill = false)
                            .fillMaxHeight(fraction = heightFraction)
                            .clip(shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(color = color)
                    )

                    Divider(color = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        DegreesChartGrid(maxDegreeCount = maxDegreeCount)
    }
}

@Composable
private fun DegreesChartGrid(maxDegreeCount: Int) {
    val textMeasurer = rememberTextMeasurer()
    val thresholdsStep = 50
    val thresholdsCount = maxDegreeCount / thresholdsStep

    Row(
        modifier = Modifier.fillMaxHeight()
    ) {
        Canvas(
            modifier = Modifier
                .weight(8f)
                .fillMaxHeight(),
            onDraw = {
                repeat(thresholdsCount) {
                    val thresholdValue = maxDegreeCount - (it + 1) * thresholdsStep
                    val y = thresholdValue / maxDegreeCount.toFloat() * size.height

                    drawLine(
                        color = Color.LightGray,
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(4.dp.toPx(), 2.dp.toPx())
                        ),
                        start = Offset(x = 0f, y = y),
                        end = Offset(x = size.width, y = y)
                    )
                }
            }
        )

        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onDraw = {
                drawLine(
                    color = Color.LightGray,
                    strokeWidth = 1.dp.toPx(),
                    start = Offset(x = 0f, y = 0f),
                    end = Offset(x = 0f, y = size.height)
                )

                repeat(thresholdsCount) {
                    val thresholdValue = (it + 1) * thresholdsStep
                    val y = (maxDegreeCount - thresholdValue) / maxDegreeCount.toFloat() * size.height

                    drawLine(
                        color = Color.LightGray,
                        strokeWidth = 1.dp.toPx(),
                        start = Offset(x = 0f, y = y),
                        end = Offset(x = 8.dp.toPx(), y = y)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = thresholdValue.toString(),
                        topLeft = Offset(x = 10.dp.toPx(), y = y - 8.sp.toPx()),
                        style = TextStyle.Default.copy(
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    )
                }
            }
        )
    }
}

@Composable
private fun DegreesLabels(degrees: Set<String>) {
    Row {
        degrees.forEach {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun GradeCountsChartPreview() {
    BoolderTheme {
        DegreeCountsChart(
            degreeCounts = mapOf(
                "1" to 3,
                "2" to 103,
                "3" to 63,
                "4" to 88,
                "5" to 60,
                "6" to 84,
                "7" to 97,
                "8" to 15,
            ),
            color = Color(red = 45, green = 161, blue = 125)
        )
    }
}
