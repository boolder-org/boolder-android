package com.boolder.boolder.view.detail.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
fun CircuitControls(
    circuitPreviousProblemId: Int?,
    circuitNextProblemId: Int?,
    onPreviousProblemClicked: () -> Unit,
    onNextProblemClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (circuitPreviousProblemId == null && circuitNextProblemId == null) return

    Row(
        modifier = modifier.padding(16.dp)
    ) {

        circuitPreviousProblemId?.let {
            CircuitControlButton(
                iconRes = R.drawable.ic_arrow_back,
                contentDescription = stringResource(id = R.string.cd_circuit_previous_boulder_problem),
                onClick = { onPreviousProblemClicked() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        circuitNextProblemId?.let {
            CircuitControlButton(
                iconRes = R.drawable.ic_arrow_forward,
                contentDescription = stringResource(id = R.string.cd_circuit_next_boulder_problem),
                onClick = { onNextProblemClicked() }
            )
        }
    }
}

@Composable
private fun CircuitControlButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Icon(
        modifier = Modifier
            .clip(shape = CircleShape)
            .background(color = Color.White, shape = CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        painter = painterResource(id = iconRes),
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.primary
    )
}

@Preview
@Composable
private fun CircuitControlsPreview() {
    BoolderTheme {
        CircuitControls(
            circuitPreviousProblemId = 9,
            circuitNextProblemId = 11,
            onPreviousProblemClicked = {},
            onNextProblemClicked = {}
        )
    }
}
