package com.boolder.boolder.data.premium.model

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.Purchase

enum class RestoreResult {
    RESTORED,
    NOT_FOUND,
    ERROR
}

fun toRestoreResult(
    responseCode: Int,
    purchases: List<Purchase>
): RestoreResult =
    when {
        responseCode != BillingResponseCode.OK -> RestoreResult.ERROR
        purchases.toPremiumStatus() == PremiumStatus.SUBSCRIBED -> RestoreResult.RESTORED
        else -> RestoreResult.NOT_FOUND
    }
