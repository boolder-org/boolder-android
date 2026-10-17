package com.boolder.boolder.view.premium

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun PaywallContent(
    viewModel: PaywallViewModel,
    presentation: PaywallPresentation,
    onCloseClicked: () -> Unit,
    onSubscribeClicked: () -> Unit,
    onContinueClicked: () -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val premiumPrice by viewModel.premiumPrice.collectAsStateWithLifecycle()
    val isPurchaseInProgress by viewModel.isPurchaseInProgress.collectAsStateWithLifecycle()
    val isPurchaseSucceeded by viewModel.isPurchaseSucceeded.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        PaywallScreen(
            presentation = presentation,
            premiumPrice = premiumPrice,
            isPurchaseInProgress = isPurchaseInProgress,
            onCloseClicked = onCloseClicked,
            onSubscribeClicked = onSubscribeClicked,
            onRestoreClicked = viewModel::onRestoreClicked,
            onTermsClicked = onTermsClicked,
            onPrivacyClicked = onPrivacyClicked
        )

        if (isPurchaseSucceeded) {
            PaywallSuccessScreen(
                modifier = Modifier.matchParentSize(),
                onContinueClicked = onContinueClicked
            )
        }
    }
}
