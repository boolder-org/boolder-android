package com.boolder.boolder.view.premium.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme

@Composable
internal fun PaywallBenefits() {
    Column {
        PaywallSectionTitle(textRes = R.string.premium_paywall_benefits_title)

        PaywallBenefit(
            iconRes = R.drawable.ic_premium_areas,
            titleRes = R.string.premium_paywall_benefit_areas_title,
            descriptionRes = R.string.premium_paywall_benefit_areas_description
        )
        PaywallBenefit(
            iconRes = R.drawable.ic_premium_problems,
            titleRes = R.string.premium_paywall_benefit_problems_title,
            descriptionRes = R.string.premium_paywall_benefit_problems_description
        )
        PaywallBenefit(
            iconRes = R.drawable.ic_premium_photos,
            titleRes = R.string.premium_paywall_benefit_photos_title,
            descriptionRes = R.string.premium_paywall_benefit_photos_description
        )
    }
}

@Composable
private fun PaywallBenefit(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int
) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = .15f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column {
            Text(
                text = stringResource(id = titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(id = descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PaywallBenefitsPreview() {
    BoolderTheme {
        Surface {
            PaywallBenefits()
        }
    }
}
