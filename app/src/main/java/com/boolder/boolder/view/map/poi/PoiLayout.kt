package com.boolder.boolder.view.map.poi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun PoiLayout(
    poiName: String,
    onOpenPoiInGoogleMaps: () -> Unit,
    onCloseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Absolute.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = poiName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenPoiInGoogleMaps
        ) {
            Text(text = stringResource(id = R.string.pois_bs_open))
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCloseClicked
        ) {
            Text(text = stringResource(id = R.string.pois_bs_close))
        }
    }
}

@PreviewLightDark
@Composable
private fun PoiLayoutPreview() {
    BoolderTheme {
        PoiLayout(
            poiName = "Parking Bois Rond Auberge",
            onOpenPoiInGoogleMaps = {},
            onCloseClicked = {}
        )
    }
}
