package com.boolder.boolder.view.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.fragment.app.Fragment
import com.boolder.boolder.R
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
                    PaywallContent(
                        modifier = Modifier
                            .padding(bottom = dimensionResource(id = R.dimen.height_bottom_nav_bar))
                            .navigationBarsPadding(),
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectPaywallEvents(
            viewModel = viewModel,
            onRestoreSucceeded = {}
        )
    }
}
