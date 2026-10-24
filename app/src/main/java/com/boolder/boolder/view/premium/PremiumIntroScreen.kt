package com.boolder.boolder.view.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.premium.composable.PaywallActions
import com.boolder.boolder.view.premium.composable.PaywallBenefits
import com.boolder.boolder.view.premium.composable.PaywallReasons

@Composable
internal fun PremiumIntroScreen(
    premiumPrice: String?,
    isPurchaseInProgress: Boolean,
    onCloseClicked: () -> Unit,
    onSubscribeClicked: () -> Unit,
    onLaterClicked: () -> Unit,
    onSeePopularAreasClicked: () -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .nestedScroll(rememberNestedScrollInteropConnection())
                .verticalScroll(rememberScrollState())
        ) {
            PremiumIntroHeader(
                onCloseClicked = onCloseClicked,
                onSeePopularAreasClicked = onSeePopularAreasClicked
            )
            PaywallBenefits()
            PaywallReasons()
        }

        PaywallActions(
            premiumPrice = premiumPrice,
            isPurchaseInProgress = isPurchaseInProgress,
            onSubscribeClicked = onSubscribeClicked,
            onRestoreClicked = null,
            onTermsClicked = onTermsClicked,
            onPrivacyClicked = onPrivacyClicked,
            onLaterClicked = onLaterClicked
        )
    }
}

@Composable
private fun PremiumIntroHeader(
    onCloseClicked: () -> Unit,
    onSeePopularAreasClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            verticalArrangement = spacedBy(4.dp)
        ) {
            Text(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.primary.copy(alpha = .15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                text = stringResource(id = R.string.premium_intro_badge).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = stringResource(id = R.string.premium_paywall_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(id = R.string.premium_paywall_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = AnnotatedString.fromHtml(
                    htmlString = stringResource(id = R.string.premium_intro_description)
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onSeePopularAreasClicked)
                    .padding(vertical = 4.dp),
                text = stringResource(id = R.string.premium_intro_see_popular_areas),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(36.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            onClick = onCloseClicked
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(id = R.string.premium_paywall_close)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PremiumIntroScreenPreview(
    @PreviewParameter(PremiumIntroScreenPreviewParameterProvider::class)
    premiumPrice: String?
) {
    BoolderTheme {
        PremiumIntroScreen(
            premiumPrice = premiumPrice,
            isPurchaseInProgress = false,
            onCloseClicked = {},
            onSubscribeClicked = {},
            onLaterClicked = {},
            onSeePopularAreasClicked = {},
            onTermsClicked = {},
            onPrivacyClicked = {}
        )
    }
}

private class PremiumIntroScreenPreviewParameterProvider : PreviewParameterProvider<String?> {
    override val values = sequenceOf(
        "10,00 €",
        null
    )
}
