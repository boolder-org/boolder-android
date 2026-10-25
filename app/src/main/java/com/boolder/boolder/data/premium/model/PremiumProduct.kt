package com.boolder.boolder.data.premium.model

import com.android.billingclient.api.ProductDetails

const val PREMIUM_BASE_PLAN_ID = "yearly"

data class PremiumProduct(
    val productDetails: ProductDetails,
    val offerToken: String,
    val formattedPrice: String
)

fun ProductDetails.toPremiumProduct(): PremiumProduct? {
    val offer = subscriptionOfferDetails
        ?.firstOrNull { it.basePlanId == PREMIUM_BASE_PLAN_ID && it.offerId == null }
        ?: return null

    val pricingPhase = offer.pricingPhases.pricingPhaseList.lastOrNull() ?: return null

    return PremiumProduct(
        productDetails = this,
        offerToken = offer.offerToken,
        formattedPrice = pricingPhase.formattedPrice
    )
}
