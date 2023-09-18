package com.boolder.boolder.view.map.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun AreaName(
    name: String?,
    onHideAreaName: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (name == null) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha animation"
    )

    Row(
        modifier = modifier
            .alpha(alpha)
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            .clickable(enabled = alpha == 1f) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .padding(4.dp)
                .clip(shape = CircleShape)
                .clickable(onClick = onHideAreaName)
                .padding(8.dp)
                .size(24.dp),
            painter = painterResource(id = R.drawable.ic_chevron_left),
            contentDescription = "Hide area name"
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            text = name.orEmpty(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Preview
@Composable
private fun AreaNamePreview() {
    BoolderTheme {
        AreaName(
            name = "Rocher canon",
            onHideAreaName = {}
        )
    }
}
