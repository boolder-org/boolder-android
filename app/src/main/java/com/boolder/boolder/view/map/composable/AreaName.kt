package com.boolder.boolder.view.map.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun AreaName(
    name: String?,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (name == null) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha animation"
    )

    Text(
        modifier = modifier
            .alpha(alpha)
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        text = name.orEmpty(),
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
private fun AreaNamePreview() {
    BoolderTheme {
        AreaName(name = "Rocher canon")
    }
}
