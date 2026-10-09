package com.boolder.boolder.view.premium.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BleauInfoBlue
import com.boolder.boolder.view.compose.BleauInfoBlueBorder
import com.boolder.boolder.view.compose.BleauInfoBlueDark
import com.boolder.boolder.view.compose.BleauInfoBlueLight
import com.boolder.boolder.view.compose.BleauInfoBlueMedium
import com.boolder.boolder.view.compose.BleauInfoBlueSoft
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun PaywallReasons() {
    val isDarkTheme = isSystemInDarkTheme()
    val shape = RoundedCornerShape(16.dp)

    Column {
        PaywallSectionTitle(textRes = R.string.premium_paywall_reasons_title)

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 6.dp)
                .fillMaxWidth()
                .background(
                    color = if (isDarkTheme) Color.BleauInfoBlue.copy(alpha = .2f) else Color.BleauInfoBlueLight,
                    shape = shape
                )
                .border(
                    width = 1.dp,
                    color = if (isDarkTheme) Color.BleauInfoBlue.copy(alpha = .4f) else Color.BleauInfoBlueBorder,
                    shape = shape
                )
                .padding(16.dp),
            verticalArrangement = spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PaywallPartnerLogo(imageRes = R.drawable.premium_logo_boolder)

                Text(
                    text = "×",
                    color = Color.BleauInfoBlueSoft
                )

                PaywallPartnerLogo(imageRes = R.drawable.premium_logo_bleau_info)

                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = stringResource(id = R.string.premium_paywall_reasons_partnership),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.BleauInfoBlueBorder else Color.BleauInfoBlueDark
                )
            }

            Text(
                text = AnnotatedString.fromHtml(
                    htmlString = stringResource(id = R.string.premium_paywall_reasons_description)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkTheme) Color.BleauInfoBlueBorder else Color.BleauInfoBlueMedium
            )
        }
    }
}

@Composable
private fun PaywallPartnerLogo(@DrawableRes imageRes: Int) {
    Image(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(color = Color.White),
        painter = painterResource(id = imageRes),
        contentDescription = null
    )
}

@PreviewLightDark
@Composable
private fun PaywallReasonsPreview() {
    BoolderTheme {
        Surface {
            PaywallReasons()
        }
    }
}
