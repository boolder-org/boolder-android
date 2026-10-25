package com.boolder.boolder.view.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.custom.EdgeToEdgeBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaywallBottomSheetDialogFragment : EdgeToEdgeBottomSheetDialogFragment() {

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
                        viewModel = viewModel,
                        presentation = PaywallPresentation.SHEET,
                        onCloseClicked = ::onCloseClicked,
                        onSubscribeClicked = { viewModel.onSubscribeClicked(requireActivity()) },
                        onContinueClicked = ::onCloseClicked,
                        onTermsClicked = ::openPaywallTerms,
                        onPrivacyClicked = ::openPaywallPrivacy
                    )
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        collectPaywallEvents(
            viewModel = viewModel,
            onRestoreSucceeded = ::onCloseClicked
        )
    }

    private fun onCloseClicked() {
        findNavController().popBackStack()
    }
}
