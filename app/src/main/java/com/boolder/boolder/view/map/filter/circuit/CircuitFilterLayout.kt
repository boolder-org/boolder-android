package com.boolder.boolder.view.map.filter.circuit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.utils.extension.composeColor
import com.boolder.boolder.view.compose.BoolderTheme
import com.mapbox.maps.CoordinateBounds

@Composable
fun CircuitFilterLayout(
    availableCircuits: List<Circuit>,
    onCircuitSelected: (Circuit?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            text = stringResource(id = R.string.circuits),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        CircuitsList(
            availableCircuits = availableCircuits,
            onCircuitSelected = onCircuitSelected
        )

        BottomButtons(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 16.dp),
            onReset = { onCircuitSelected(null) }
        )
    }
}

@Composable
private fun CircuitsList(
    availableCircuits: List<Circuit>,
    onCircuitSelected: (Circuit) -> Unit
) {
    val shape = MaterialTheme.shapes.small

    Column(
        modifier = Modifier
            .clip(shape = shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = shape
            )
    ) {
        availableCircuits.forEachIndexed { index, circuit ->
            CircuitItem(
                modifier = Modifier.clickable { onCircuitSelected(circuit) },
                circuit = circuit
            )

            if (index < availableCircuits.lastIndex) {
                Divider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun CircuitItem(
    circuit: Circuit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = spacedBy(8.dp),
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
                tint = Color(red = 1f, .5f, 0f)
            )
        }

        Text(
            text = circuit.averageGrade,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BottomButtons(
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(8.dp)
    ) {
        Button(
            colors = ButtonDefaults.outlinedButtonColors(),
            border = ButtonDefaults.outlinedButtonBorder,
            onClick = onReset
        ) {
            Text(text = stringResource(id = R.string.reset))
        }
    }
}

@Preview
@Composable
private fun CircuitFilterLayoutPreview() {
    val availableCircuits = CircuitColor.entries
        .take(9)
        .mapIndexed { index, circuitColor ->
            Circuit(
                id = index,
                color = circuitColor,
                averageGrade = "${index + 1}a",
                isBeginnerFriendly = index == 0,
                isDangerous = index == 8,
                coordinateBounds = CoordinateBounds.world()
            )
        }

    BoolderTheme {
        CircuitFilterLayout(
            availableCircuits = availableCircuits,
            onCircuitSelected = {}
        )
    }
}
