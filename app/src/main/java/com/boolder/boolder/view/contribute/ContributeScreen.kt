package com.boolder.boolder.view.contribute

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderBlue
import com.boolder.boolder.view.compose.BoolderOrange
import com.boolder.boolder.view.compose.BoolderTeal
import com.boolder.boolder.view.compose.BoolderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContributeScreen(
    onStartContributingClicked: () -> Unit,
    onLearnMoreClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .padding(bottom = dimensionResource(id = R.dimen.height_bottom_nav_bar))
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.contribute_title))
                }
            )
        },
        content = {
            ContributeScreenContent(
                contentPadding = it,
                onStartContributingClicked = onStartContributingClicked,
                onLearnMoreClicked = onLearnMoreClicked
            )
        }
    )
}

@Composable
private fun ContributeScreenContent(
    contentPadding: PaddingValues,
    onStartContributingClicked: () -> Unit,
    onLearnMoreClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        item {
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = stringResource(id = R.string.contribute_intro),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }

        item { ContributeSteps() }

        item {
            ContributeActions(
                modifier = Modifier.padding(vertical = 16.dp),
                onStartContributingClicked = onStartContributingClicked,
                onLearnMoreClicked = onLearnMoreClicked
            )
        }
    }
}

@Composable
private fun ContributeSteps() {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = spacedBy(16.dp)
        ) {
            ContributeItem(
                textRes = R.string.contribute_photo,
                iconRes = R.drawable.ic_camera_alt,
                iconTint = MaterialTheme.colorScheme.primary
            )
            ContributeItem(
                textRes = R.string.contribute_location,
                iconRes = R.drawable.ic_location_on,
                iconTint = Color.BoolderBlue
            )
            ContributeItem(
                textRes = R.string.contribute_line,
                iconRes = R.drawable.ic_route,
                iconTint = Color.BoolderTeal
            )
            ContributeItem(
                textRes = R.string.contribute_report,
                iconRes = R.drawable.ic_outline_report,
                iconTint = Color.BoolderOrange
            )
        }
    }
}

@Composable
private fun ContributeItem(
    @StringRes textRes: Int,
    @DrawableRes iconRes: Int,
    iconTint: Color
) {
    Row(
        horizontalArrangement = spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconTint
        )
        Text(
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ContributeActions(
    onStartContributingClicked: () -> Unit,
    onLearnMoreClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStartContributingClicked
        ) {
            Text(text = stringResource(id = R.string.contribute_cta))
        }

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLearnMoreClicked
        ) {
            Text(text = stringResource(id = R.string.contribute_learn_more))
        }
    }
}

@PreviewLightDark
@Composable
private fun ContributeScreenPreview() {
    BoolderTheme {
        ContributeScreen(
            onStartContributingClicked = {},
            onLearnMoreClicked = {}
        )
    }
}
