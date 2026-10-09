package com.boolder.boolder.view.premium

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.utils.getLanguage
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
                    val premiumPrice by viewModel.premiumPrice.collectAsStateWithLifecycle()

                    PaywallScreen(
                        presentation = PaywallPresentation.SHEET,
                        premiumPrice = premiumPrice,
                        onCloseClicked = ::onCloseClicked,
                        onSubscribeClicked = {}, // TODO(premium): D2
                        onRestoreClicked = {}, // TODO(premium): D4
                        onTermsClicked = ::onTermsClicked,
                        onPrivacyClicked = ::onPrivacyClicked
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

    private fun openWebUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Log.i("Paywall", "No apps can handle this kind of intent")
        }
    }
}
