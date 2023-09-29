package com.boolder.boolder.view.detail.composable

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.Line
import com.boolder.boolder.utils.CubicCurveAlgorithm
import com.boolder.boolder.utils.extension.composeColor
import com.boolder.boolder.utils.previewgenerator.dummyLine
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.detail.PointD

@Composable
internal fun ProblemLine(
    line: Line?,
    color: Color,
    modifier: Modifier = Modifier
) {
    val points = line?.points() ?: return

    BoxWithConstraints(
        modifier = modifier
    ) {
        val path by remember { mutableStateOf(Path()) }
        var pathLength by remember { mutableFloatStateOf(0f) }
        var internalColor by remember { mutableStateOf(Color.Transparent) }
        val lineLengthRatio = remember { Animatable(0f) }

        val width = constraints.maxWidth
        val height = constraints.maxHeight

        LaunchedEffect(key1 = points, key2 = color) {
            internalColor = Color.Transparent
            path.reset()

            path.addCubicCurves(
                points = points,
                containerWidth = width,
                containerHeight = height
            )

            pathLength = PathMeasure()
                .apply { setPath(path = path, forceClosed = false) }
                .length

            lineLengthRatio.snapTo(0f)
            internalColor = color
            lineLengthRatio.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, delayMillis = 300)
            )
        }

        Canvas(
            modifier = Modifier.fillMaxSize(),
            onDraw = {
                drawPath(
                    path = path,
                    color = internalColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(
                                pathLength * lineLengthRatio.value,
                                pathLength * 1f
                            ),
                            phase = 0f
                        )
                    )
                )
            }
        )
    }
}

private fun Path.addCubicCurves(
    points: List<PointD>,
    containerWidth: Int,
    containerHeight: Int
) = apply {
    val controlPoints = try {
        CubicCurveAlgorithm().controlPointsFromPoints(points)
    } catch (e: Exception) {
        Log.e("TAG", e.message.orEmpty())
        return@apply
    }

    points.firstOrNull()?.let {
        moveTo(
            x = it.x.toFloat() * containerWidth,
            y = it.y.toFloat() * containerHeight
        )
    }

    points.forEachIndexed { index, point ->
        if (index == 0) return@forEachIndexed

        val (controlPoint1, controlPoint2) = controlPoints.getOrNull(index - 1)
            ?: return@forEachIndexed

        cubicTo(
            x1 = controlPoint1.x.toFloat() * containerWidth,
            y1 = controlPoint1.y.toFloat() * containerHeight,
            x2 = controlPoint2.x.toFloat() * containerWidth,
            y2 = controlPoint2.y.toFloat() * containerHeight,
            x3 = point.x.toFloat() * containerWidth,
            y3 = point.y.toFloat() * containerHeight
        )
    }
}

/**
 * Use the preview interactive mode in order to make the line appearing
 */
@Preview
@Composable
private fun ProblemLinePreview() {
    BoolderTheme {
        ProblemLine(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(color = Color.White),
            line = dummyLine(),
            color = CircuitColor.RED.composeColor()
        )
    }
}

