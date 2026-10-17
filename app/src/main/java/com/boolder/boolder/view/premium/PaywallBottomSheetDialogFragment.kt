package com.boolder.boolder.view.premium

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.R
import com.boolder.boolder.utils.extension.launchAndCollectIn
import com.boolder.boolder.utils.getLanguage
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.custom.EdgeToEdgeBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
                    val premiumPrice by viewModel.premiumPrice.collectAsStateWithLifecycle()
                    val isPurchaseInProgress by viewModel.isPurchaseInProgress.collectAsStateWithLifecycle()
                    val isPurchaseSucceeded by viewModel.isPurchaseSucceeded.collectAsStateWithLifecycle()

                    Box {
                        PaywallScreen(
                            presentation = PaywallPresentation.SHEET,
                            premiumPrice = premiumPrice,
                            isPurchaseInProgress = isPurchaseInProgress,
                            onCloseClicked = ::onCloseClicked,
                            onSubscribeClicked = { viewModel.onSubscribeClicked(requireActivity()) },
                            onRestoreClicked = viewModel::onRestoreClicked,
                            onTermsClicked = ::onTermsClicked,
                            onPrivacyClicked = ::onPrivacyClicked
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

        viewModel.events.launchAndCollectIn(owner = viewLifecycleOwner) { event ->
            when (event) {
                is PaywallViewModel.Event.PurchasePending -> showDialog(
                    titleRes = R.string.premium_purchase_pending_title,
                    messageRes = R.string.premium_purchase_pending_message
                )

                is PaywallViewModel.Event.PurchaseFailed -> showDialog(
                    titleRes = R.string.premium_purchase_error_title,
                    messageRes = R.string.premium_purchase_error_message
                )

                is PaywallViewModel.Event.RestoreSucceeded -> showDialog(
                    titleRes = R.string.premium_paywall_restore,
                    messageRes = R.string.premium_restore_success_message,
                    onDismiss = ::onCloseClicked
                )

                is PaywallViewModel.Event.RestoreNotFound -> showDialog(
                    titleRes = R.string.premium_paywall_restore,
                    messageRes = R.string.premium_restore_not_found_message
                )

                is PaywallViewModel.Event.RestoreFailed -> showDialog(
                    titleRes = R.string.premium_paywall_restore,
                    messageRes = R.string.premium_restore_error_message
                )
            }
        }
    }

    private fun onCloseClicked() {
        findNavController().popBackStack()
    }

    private fun onTermsClicked() {
        // TODO(premium): this page does not exist yet on boolder.com
        openWebUrl("https://www.boolder.com/${getLanguage()}/terms")
    }

    private fun onPrivacyClicked() {
        openWebUrl("https://www.boolder.com/${getLanguage()}/privacy")
    }

    private fun showDialog(titleRes: Int, messageRes: Int, onDismiss: () -> Unit = {}) {
        val context = context ?: return

        MaterialAlertDialogBuilder(context)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener { onDismiss() }
            .show()
    }

    private fun openWebUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Log.i("Paywall", "No apps can handle this kind of intent")
        }
    }
}
