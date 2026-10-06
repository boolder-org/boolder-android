package com.boolder.boolder.data.premium.model

import com.android.billingclient.api.Purchase
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class PremiumStatusTest {

    @Test
    fun `toPremiumStatus() should return NOT_SUBSCRIBED when there is no purchase`() {
        // Given
        val purchases = emptyList<Purchase>()

        // When
        val status = purchases.toPremiumStatus()

        // Then
        assertEquals(PremiumStatus.NOT_SUBSCRIBED, status)
    }

    @Test
    fun `toPremiumStatus() should return SUBSCRIBED when the premium product is purchased`() {
        // Given
        val purchases = listOf(
            purchase(PREMIUM_PRODUCT_ID, Purchase.PurchaseState.PURCHASED)
        )

        // When
        val status = purchases.toPremiumStatus()

        // Then
        assertEquals(PremiumStatus.SUBSCRIBED, status)
    }

    @Test
    fun `toPremiumStatus() should return NOT_SUBSCRIBED when the premium purchase is pending`() {
        // Given
        val purchases = listOf(
            purchase(PREMIUM_PRODUCT_ID, Purchase.PurchaseState.PENDING)
        )

        // When
        val status = purchases.toPremiumStatus()

        // Then
        assertEquals(PremiumStatus.NOT_SUBSCRIBED, status)
    }

    @Test
    fun `toPremiumStatus() should return NOT_SUBSCRIBED when another product is purchased`() {
        // Given
        val purchases = listOf(
            purchase("another_product", Purchase.PurchaseState.PURCHASED)
        )

        // When
        val status = purchases.toPremiumStatus()

        // Then
        assertEquals(PremiumStatus.NOT_SUBSCRIBED, status)
    }

    @Test
    fun `toPremiumStatus() should return SUBSCRIBED when one of several purchases is the purchased premium product`() {
        // Given
        val purchases = listOf(
            purchase("another_product", Purchase.PurchaseState.PURCHASED),
            purchase(PREMIUM_PRODUCT_ID, Purchase.PurchaseState.PENDING),
            purchase(PREMIUM_PRODUCT_ID, Purchase.PurchaseState.PURCHASED)
        )

        // When
        val status = purchases.toPremiumStatus()

        // Then
        assertEquals(PremiumStatus.SUBSCRIBED, status)
    }

    private fun purchase(productId: String, purchaseState: Int): Purchase =
        mock {
            on { products } doReturn listOf(productId)
            on { this.purchaseState } doReturn purchaseState
        }
}
