package com.boolder.boolder.data.premium.model

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.Purchase

enum class PurchaseResult {
    SUCCESS,
    CANCELED,
    PENDING,
    ALREADY_OWNED,
    ERROR
}

fun toPurchaseResult(
    responseCode: Int,
    purchases: List<Purchase>
): PurchaseResult =
    when (responseCode) {
        BillingResponseCode.OK -> when {
            purchases.toPremiumStatus() == PremiumStatus.SUBSCRIBED -> PurchaseResult.SUCCESS
            purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING } -> PurchaseResult.PENDING
            else -> PurchaseResult.ERROR
        }
        BillingResponseCode.USER_CANCELED -> PurchaseResult.CANCELED
        BillingResponseCode.ITEM_ALREADY_OWNED -> PurchaseResult.ALREADY_OWNED
        else -> PurchaseResult.ERROR
    }
