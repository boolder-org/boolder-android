package com.boolder.boolder.view.map.filter.steepness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Steepness
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.MapViewModel

@Composable
internal fun SteepnessFilterLayout(
    selectedSteepnesses: List<String>,
    onSteepnessToggled: (String) -> Unit,
    onSteepnessesReset: () -> Unit,
    onSteepnessesValidated: () -> Unit,
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
            text = stringResource(id = R.string.steepness),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        SteepnessChips(
            selectedSteepnesses = selectedSteepnesses,
            onSteepnessToggled = onSteepnessToggled
        )

        BottomButtons(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 16.dp),
            onSteepnessesReset = onSteepnessesReset,
            onSteepnessesValidated = onSteepnessesValidated
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SteepnessChips(
    selectedSteepnesses: List<String>,
    onSteepnessToggled: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(8.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {
        Steepness.values().forEach { steepness ->
            val textValue = steepness.name.lowercase()

            SteepnessChip(
                steepness = steepness,
                selected = textValue in selectedSteepnesses,
                onClick = { onSteepnessToggled(textValue) }
            )
        }
    }
}

@Composable
private fun SteepnessChip(
    steepness: Steepness,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        label = { Text(text = stringResource(id = steepness.textRes)) },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = steepness.iconRes),
                contentDescription = null
            )
        },
        onClick = onClick
    )
}

@Composable
private fun BottomButtons(
    onSteepnessesReset: () -> Unit,
    onSteepnessesValidated: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(8.dp)
    ) {
        Button(
            colors = ButtonDefaults.outlinedButtonColors(),
            border = ButtonDefaults.outlinedButtonBorder(),
            onClick = onSteepnessesReset
        ) {
            Text(text = stringResource(id = R.string.reset))
        }

        Button(
            onClick = onSteepnessesValidated
        ) {
            Text(text = stringResource(id = R.string.apply))
        }
    }
}

@PreviewLightDark
@Composable
internal fun SteepnessFilterLayoutPreview() {
    BoolderTheme {
        SteepnessFilterLayout(
            selectedSteepnesses = MapViewModel.ALL_STEEPNESSES,
            onSteepnessToggled = {},
            onSteepnessesReset = {},
            onSteepnessesValidated = {}
        )
    }
}
