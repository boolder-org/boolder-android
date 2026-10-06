package com.boolder.boolder.data.premium.model

import com.android.billingclient.api.Purchase

const val PREMIUM_PRODUCT_ID = "boolder_premium_yearly"

enum class PremiumStatus {
    NOT_SUBSCRIBED,
    SUBSCRIBED
}

fun List<Purchase>.toPremiumStatus(): PremiumStatus {
    val isSubscribed = any {
        PREMIUM_PRODUCT_ID in it.products && it.purchaseState == Purchase.PurchaseState.PURCHASED
    }

    return if (isSubscribed) PremiumStatus.SUBSCRIBED else PremiumStatus.NOT_SUBSCRIBED
}
