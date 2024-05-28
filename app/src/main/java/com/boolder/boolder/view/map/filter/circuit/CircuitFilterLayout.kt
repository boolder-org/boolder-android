package com.boolder.boolder.view.map.filter.circuit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.view.compose.BoolderOrange
import com.boolder.boolder.view.compose.BoolderRippleTheme
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.compose.CircuitItem
import com.mapbox.geojson.Point

@Composable
fun CircuitFilterLayout(
    availableCircuits: List<Circuit>,
    onCircuitSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            text = stringResource(id = R.string.circuits),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (availableCircuits.isEmpty()) {
            CircuitsEmptyContent()
        } else {
            CircuitsContent(
                availableCircuits = availableCircuits,
                onCircuitSelected = onCircuitSelected
            )
        }
    }
}

@Composable
private fun CircuitsEmptyContent() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = R.drawable.ic_outline_wrong_location),
            contentDescription = null,
            tint = Color.BoolderOrange
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.no_circuit_available),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CircuitsContent(
    availableCircuits: List<Circuit>,
    onCircuitSelected: (Int) -> Unit
) {
    Column {
        CompositionLocalProvider(LocalRippleTheme provides BoolderRippleTheme) {
            CircuitsList(
                availableCircuits = availableCircuits,
                onCircuitSelected = onCircuitSelected
            )
        }

        BottomButtons(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 16.dp),
            onReset = { onCircuitSelected(-1) }
        )
    }
}

@Composable
private fun CircuitsList(
    availableCircuits: List<Circuit>,
    onCircuitSelected: (Int) -> Unit
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
                modifier = Modifier.clickable { onCircuitSelected(circuit.id) },
                circuit = circuit
            )

            if (index < availableCircuits.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
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

@PreviewLightDark
@Composable
private fun CircuitFilterLayoutPreview(
    @PreviewParameter(CircuitFilterLayoutPreviewParameterProvider::class)
    circuits: List<Circuit>
) {
    BoolderTheme {
        CircuitFilterLayout(
            availableCircuits = circuits,
            onCircuitSelected = {}
        )
    }
}

private class CircuitFilterLayoutPreviewParameterProvider : PreviewParameterProvider<List<Circuit>> {
    val circuits = CircuitColor.entries
        .take(9)
        .mapIndexed { index, circuitColor ->
            Circuit(
                id = index,
                color = circuitColor,
                averageGrade = "${index + 1}a",
                isBeginnerFriendly = index == 0,
                isDangerous = index == 8,
                coordinateBounds = listOf(
                    Point.fromLngLat(0.0, 0.0),
                    Point.fromLngLat(0.0, 0.0)
                )
            )
        }

    override val values = sequenceOf(emptyList(), circuits)
}
