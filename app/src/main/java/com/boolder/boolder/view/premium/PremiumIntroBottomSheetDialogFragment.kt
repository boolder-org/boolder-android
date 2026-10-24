package com.boolder.boolder.view.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.R
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.custom.EdgeToEdgeBottomSheetDialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class PremiumIntroBottomSheetDialogFragment : EdgeToEdgeBottomSheetDialogFragment() {

    private val viewModel by viewModel<PaywallViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val premiumPrice by viewModel.premiumPrice.collectAsStateWithLifecycle()
                    val isPurchaseInProgress by viewModel.isPurchaseInProgress.collectAsStateWithLifecycle()
                    val isPurchaseSucceeded by viewModel.isPurchaseSucceeded.collectAsStateWithLifecycle()

                    Box {
                        PremiumIntroScreen(
                            premiumPrice = premiumPrice,
                            isPurchaseInProgress = isPurchaseInProgress,
                            onCloseClicked = ::onCloseClicked,
                            onSubscribeClicked = { viewModel.onSubscribeClicked(requireActivity()) },
                            onLaterClicked = ::onCloseClicked,
                            onSeePopularAreasClicked = ::onSeePopularAreasClicked,
                            onTermsClicked = ::openPaywallTerms,
                            onPrivacyClicked = ::openPaywallPrivacy
                        )

                        if (isPurchaseSucceeded) {
                            PaywallSuccessScreen(
                                modifier = Modifier.matchParentSize(),
                                onContinueClicked = ::onCloseClicked
                            )
                        }
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        if (savedInstanceState == null) viewModel.onIntroShown()

        collectPaywallEvents(
            viewModel = viewModel,
            onRestoreSucceeded = ::onCloseClicked,
            onPurchasePending = ::onCloseClicked
        )
    }

    private fun onCloseClicked() {
        findNavController().popBackStack()
    }

    private fun onSeePopularAreasClicked() {
        onCloseClicked()

        requireActivity()
            .findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            .selectedItemId = R.id.discover_fragment
    }
}
