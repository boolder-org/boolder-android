package com.boolder.boolder.view.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.premium.composable.PaywallLinks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PremiumManagementScreen(
    premiumPrice: String?,
    onManageClicked: () -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.tab_premium))
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(16.dp)
        ) {
            PremiumStatusCard(premiumPrice = premiumPrice)

            PremiumManageButton(onClick = onManageClicked)

            Text(
                text = stringResource(id = R.string.premium_paywall_legal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            PaywallLinks(
                onRestoreClicked = null,
                onTermsClicked = onTermsClicked,
                onPrivacyClicked = onPrivacyClicked
            )
        }
    }
}

@Composable
private fun PremiumStatusCard(premiumPrice: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        verticalArrangement = spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.premium_paywall_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color = Color.White.copy(alpha = .25f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                text = stringResource(id = R.string.premium_management_active),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (premiumPrice != null) {
            Text(
                text = stringResource(id = R.string.premium_management_price, premiumPrice),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = .9f)
            )
        }
    }
}

@Composable
private fun PremiumManageButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.ic_credit_card),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.premium_management_manage),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "↗",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@PreviewLightDark
@Composable
private fun PremiumManagementScreenPreview(
    @PreviewParameter(PremiumManagementScreenPreviewParameterProvider::class)
    premiumPrice: String?
) {
    BoolderTheme {
        PremiumManagementScreen(
            premiumPrice = premiumPrice,
            onManageClicked = {},
            onTermsClicked = {},
            onPrivacyClicked = {}
        )
    }
}

private class PremiumManagementScreenPreviewParameterProvider : PreviewParameterProvider<String?> {
    override val values = sequenceOf(
        "10,00 €",
        null
    )
}
