package com.boolder.boolder.view.areadetails.areaoverview.composable.acessfrompoi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.AccessFromPoi
import com.boolder.boolder.domain.model.PoiTransport
import com.boolder.boolder.domain.model.PoiType
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun AccessFromPoi(
    accessFromPoi: AccessFromPoi,
    onPoiClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onPoiClicked(accessFromPoi.googleUrl) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.Absolute.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(id = accessFromPoi.type.iconRes),
            contentDescription = null,
            tint = when (accessFromPoi.type.iconRes) {
                PoiType.PARKING.iconRes -> Color(red = 42, green = 86, blue = 154)
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        Text(
            modifier = Modifier.weight(1f),
            text = accessFromPoi.name
        )

        Icon(
            painter = painterResource(id = accessFromPoi.transport.iconRes),
            contentDescription = null
        )

        Text(
            text = stringResource(
                id = R.string.poi_distance_in_min,
                accessFromPoi.distanceInMinutes
            )
        )
    }
}

@Preview
@Composable
private fun AccessFromPoiPreview(
    @PreviewParameter(AccessFromPoiPreviewParameterProvider::class)
    accessFromPoi: AccessFromPoi
) {
    BoolderTheme {
        AccessFromPoi(
            modifier = Modifier.background(color = Color.White),
            accessFromPoi = accessFromPoi,
            onPoiClicked = {}
        )
    }
}

private class AccessFromPoiPreviewParameterProvider : PreviewParameterProvider<AccessFromPoi> {
    override val values = sequenceOf(
        AccessFromPoi(
            distanceInMinutes = 2,
            transport = PoiTransport.WALKING,
            type = PoiType.PARKING,
            name = "Rocher Canon",
            googleUrl = ""
        ),
        AccessFromPoi(
            distanceInMinutes = 15,
            transport = PoiTransport.BIKE,
            type = PoiType.TRAIN_STATION,
            name = "Bois-le-Roi",
            googleUrl = ""
        ),
        AccessFromPoi(
            distanceInMinutes = 35,
            transport = PoiTransport.WALKING,
            type = PoiType.TRAIN_STATION,
            name ="Bois-le-Roi",
            googleUrl = ""
        )
    )
}
