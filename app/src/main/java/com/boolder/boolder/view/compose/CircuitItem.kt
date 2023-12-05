package com.boolder.boolder.view.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.utils.extension.composeColor
import com.mapbox.geojson.Point
import com.mapbox.maps.CoordinateBounds

@Composable
internal fun CircuitItem(
    circuit: Circuit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Absolute.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color = circuit.color.composeColor(), shape = CircleShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
        )

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.circuit, circuit.color.localizedName()),
            style = MaterialTheme.typography.bodyMedium
                .copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (circuit.isBeginnerFriendly) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sentiment_satisfied_alt),
                contentDescription = null,
                tint = Color(red = .4f, .76f, .4f)
            )
        }

        if (circuit.isDangerous) {
            Icon(
                painter = painterResource(id = R.drawable.ic_error_outline),
                contentDescription = null,
                tint = Color.Orange
            )
        }

        Text(
            text = circuit.averageGrade,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun CircuitItemPreview(
    @PreviewParameter(CircuitItemPreviewParameterProvider::class)
    circuit: Circuit
) {
    BoolderTheme {
        CircuitItem(
            modifier = Modifier
                .background(color = Color.White)
                .padding(16.dp),
            circuit = circuit
        )
    }
}

private class CircuitItemPreviewParameterProvider : PreviewParameterProvider<Circuit> {
    override val values = CircuitColor.entries
        .take(9)
        .mapIndexed { index, circuitColor ->
            Circuit(
                id = index,
                color = circuitColor,
                averageGrade = "${index + 1}a",
                isBeginnerFriendly = index == 0,
                isDangerous = index == 8,
                coordinateBounds = CoordinateBounds(
                    Point.fromLngLat(0.0, 0.0),
                    Point.fromLngLat(0.0, 0.0)
                )
            )
        }
        .asSequence()
}
