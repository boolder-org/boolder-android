package com.boolder.boolder.view.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.dimensionResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boolder.boolder.R
import com.boolder.boolder.data.premium.model.PremiumStatus
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class PremiumFragment : Fragment() {

    private val viewModel by viewModel<PaywallViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val premiumStatus by viewModel.premiumStatus.collectAsStateWithLifecycle()
                    val premiumPrice by viewModel.premiumPrice.collectAsStateWithLifecycle()
                    val isPurchaseSucceeded by viewModel.isPurchaseSucceeded.collectAsStateWithLifecycle()

                    val modifier = Modifier
                        .padding(bottom = dimensionResource(id = R.dimen.height_bottom_nav_bar))
                        .navigationBarsPadding()

                    if (premiumStatus == PremiumStatus.SUBSCRIBED && !isPurchaseSucceeded) {
                        PremiumManagementScreen(
                            modifier = modifier,
                            premiumPrice = premiumPrice,
                            onManageClicked = ::openPremiumManagement,
                            onTermsClicked = ::openPaywallTerms,
                            onPrivacyClicked = ::openPaywallPrivacy
                        )
                    } else {
                        PaywallContent(
                            modifier = modifier,
                            viewModel = viewModel,
                            presentation = PaywallPresentation.TAB,
                            onCloseClicked = {},
                            onSubscribeClicked = { viewModel.onSubscribeClicked(requireActivity()) },
                            onContinueClicked = viewModel::onSuccessDismissed,
                            onTermsClicked = ::openPaywallTerms,
                            onPrivacyClicked = ::openPaywallPrivacy
                        )
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectPaywallEvents(
            viewModel = viewModel,
            onRestoreSucceeded = {}
        )
    }
}
