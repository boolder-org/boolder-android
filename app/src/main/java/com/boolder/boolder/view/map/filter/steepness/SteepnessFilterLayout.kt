package com.boolder.boolder.view.map.filter.steepness

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Steepness
import com.boolder.boolder.view.compose.BoolderRippleTheme
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
fun SteepnessFilterLayout(
    onSteepnessClicked: (Int) -> Unit,
    onSteepnessReset: () -> Unit,
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
            .navigationBarsPadding(),
        verticalArrangement = spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = stringResource(id = R.string.steepness_type),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        CompositionLocalProvider(LocalRippleTheme provides BoolderRippleTheme) {
            SteepnessTypes(
                onSteepnessClicked = onSteepnessClicked
            )
        }

        Button(
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.outlinedButtonColors(),
            border = ButtonDefaults.outlinedButtonBorder,
            onClick = onSteepnessReset
        ) {
            Text(text = stringResource(id = R.string.reset))
        }
    }
}

@Composable
private fun SteepnessTypes(
    onSteepnessClicked: (Int) -> Unit
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
        val steepnessTypes = Steepness.entries

        steepnessTypes.forEachIndexed { index, steepness ->
            SteepnessItem(
                modifier = Modifier.clickable { onSteepnessClicked(index) },
                steepness = steepness
            )

            if (index < steepnessTypes.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun SteepnessItem(
    steepness: Steepness,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = steepness.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = steepness.textRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@PreviewLightDark
@Composable
private fun SteepnessFilterLayoutPreview() {
    BoolderTheme {
        SteepnessFilterLayout(
            onSteepnessClicked = {},
            onSteepnessReset = {}
        )
    }
}
