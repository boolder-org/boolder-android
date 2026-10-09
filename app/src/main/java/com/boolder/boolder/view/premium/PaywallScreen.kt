package com.boolder.boolder.view.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.premium.composable.PaywallActions
import com.boolder.boolder.view.premium.composable.PaywallBenefits
import com.boolder.boolder.view.premium.composable.PaywallHeader
import com.boolder.boolder.view.premium.composable.PaywallReasons

enum class PaywallPresentation {
    SHEET,
    TAB
}

@Composable
internal fun PaywallScreen(
    presentation: PaywallPresentation,
    premiumPrice: String?,
    isPurchaseInProgress: Boolean,
    onCloseClicked: () -> Unit,
    onSubscribeClicked: () -> Unit,
    onRestoreClicked: () -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .nestedScroll(rememberNestedScrollInteropConnection())
                .verticalScroll(rememberScrollState())
        ) {
            PaywallHeader(
                showCloseButton = presentation == PaywallPresentation.SHEET,
                onCloseClicked = onCloseClicked
            )

            PaywallBenefits()
            PaywallReasons()
        }

        PaywallActions(
            premiumPrice = premiumPrice,
            isPurchaseInProgress = isPurchaseInProgress,
            onSubscribeClicked = onSubscribeClicked,
            onRestoreClicked = onRestoreClicked,
            onTermsClicked = onTermsClicked,
            onPrivacyClicked = onPrivacyClicked
        )
    }
}

@PreviewLightDark
@Composable
private fun PaywallScreenPreview(
    @PreviewParameter(PaywallScreenPreviewParameterProvider::class)
    premiumPrice: String?
) {
    BoolderTheme {
        PaywallScreen(
            presentation = PaywallPresentation.SHEET,
            premiumPrice = premiumPrice,
            isPurchaseInProgress = false,
            onCloseClicked = {},
            onSubscribeClicked = {},
            onRestoreClicked = {},
            onTermsClicked = {},
            onPrivacyClicked = {}
        )
    }
}

private class PaywallScreenPreviewParameterProvider : PreviewParameterProvider<String?> {
    override val values = sequenceOf(
        "10,00 €",
        null
    )
}
