package com.boolder.boolder.view.premium.composable

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boolder.boolder.view.compose.BoolderTheme
import kotlin.random.Random

private const val CONFETTI_COUNT = 18

private val confettiColors = listOf(Color(0xFFFFD166), Color(0xFFEF476F), Color.White)

private val confettiWidth = 9.dp
private val confettiHeight = 14.dp

@Composable
internal fun PaywallSuccessConfetti(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val areAnimationsEnabled = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }

    if (!areAnimationsEnabled) return

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        repeat(CONFETTI_COUNT) { index ->
            ConfettiPiece(
                color = confettiColors[index % confettiColors.size],
                containerWidth = maxWidth,
                containerHeight = maxHeight
            )
        }
    }
}

@Composable
private fun ConfettiPiece(
    color: Color,
    containerWidth: Dp,
    containerHeight: Dp
) {
    // random values are remembered so that each piece keeps them across recompositions
    val fall = remember { ConfettiFall.random() }

    val progress by rememberInfiniteTransition(label = "confetti").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = fall.durationMillis, easing = LinearEasing),
            initialStartOffset = StartOffset(fall.startDelayMillis)
        ),
        label = "confetti fall"
    )

    // the fall starts and ends off screen, so the jump back to the top
    // on each repeat is never visible
    Box(
        modifier = Modifier
            .size(width = confettiWidth, height = confettiHeight)
            .graphicsLayer {
                translationX = (containerWidth * fall.column + fall.drift * progress - confettiWidth / 2).toPx()
                translationY = ((-20).dp + (containerHeight + 40.dp) * progress - confettiHeight / 2).toPx()
                rotationZ = fall.startAngle + fall.spin * progress
            }
            .background(color = color, shape = RoundedCornerShape(3.dp))
    )
}

private data class ConfettiFall(
    val column: Float,
    val durationMillis: Int,
    val startDelayMillis: Int,
    val drift: Dp,
    val startAngle: Float,
    val spin: Float
) {
    companion object {
        fun random() = ConfettiFall(
            column = Random.nextFloat(),
            durationMillis = Random.nextInt(from = 4_000, until = 8_001),
            startDelayMillis = Random.nextInt(from = 0, until = 4_001),
            drift = Random.nextInt(from = -70, until = 71).dp,
            startAngle = Random.nextFloat() * 360f,
            spin = (360f + Random.nextFloat() * 720f) * if (Random.nextBoolean()) 1 else -1
        )
    }
}

@Preview
@Composable
private fun PaywallSuccessConfettiPreview() {
    BoolderTheme {
        PaywallSuccessConfetti(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.primary)
        )
    }
}
