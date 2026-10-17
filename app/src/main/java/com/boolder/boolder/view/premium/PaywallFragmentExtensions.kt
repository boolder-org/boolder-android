package com.boolder.boolder.view.premium

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.boolder.boolder.R
import com.boolder.boolder.utils.extension.launchAndCollectIn
import com.boolder.boolder.utils.getLanguage
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal fun Fragment.collectPaywallEvents(
    viewModel: PaywallViewModel,
    onRestoreSucceeded: () -> Unit
) {
    viewModel.events.launchAndCollectIn(owner = viewLifecycleOwner) { event ->
        when (event) {
            is PaywallViewModel.Event.PurchasePending -> showPaywallDialog(
                titleRes = R.string.premium_purchase_pending_title,
                messageRes = R.string.premium_purchase_pending_message
            )

            is PaywallViewModel.Event.PurchaseFailed -> showPaywallDialog(
                titleRes = R.string.premium_purchase_error_title,
                messageRes = R.string.premium_purchase_error_message
            )

            is PaywallViewModel.Event.RestoreSucceeded -> showPaywallDialog(
                titleRes = R.string.premium_paywall_restore,
                messageRes = R.string.premium_restore_success_message,
                onDismiss = onRestoreSucceeded
            )

            is PaywallViewModel.Event.RestoreNotFound -> showPaywallDialog(
                titleRes = R.string.premium_paywall_restore,
                messageRes = R.string.premium_restore_not_found_message
            )

            is PaywallViewModel.Event.RestoreFailed -> showPaywallDialog(
                titleRes = R.string.premium_paywall_restore,
                messageRes = R.string.premium_restore_error_message
            )
        }
    }
}

internal fun Fragment.openPaywallTerms() {
    // TODO(premium): this page does not exist yet on boolder.com
    openWebUrl("https://www.boolder.com/${getLanguage()}/terms")
}

internal fun Fragment.openPaywallPrivacy() {
    openWebUrl("https://www.boolder.com/${getLanguage()}/privacy")
}

private fun Fragment.showPaywallDialog(titleRes: Int, messageRes: Int, onDismiss: () -> Unit = {}) {
    val context = context ?: return

    MaterialAlertDialogBuilder(context)
        .setTitle(titleRes)
        .setMessage(messageRes)
        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        .setOnDismissListener { onDismiss() }
        .show()
}

private fun Fragment.openWebUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())

    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Log.i("Paywall", "No apps can handle this kind of intent")
    }
}
