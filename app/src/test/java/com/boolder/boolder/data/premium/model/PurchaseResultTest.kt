package com.boolder.boolder.data.premium.model

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.Purchase
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class PurchaseResultTest {

    @Test
    fun `toPurchaseResult() should return SUCCESS when the premium product is purchased`() {
        // Given
        val purchases = listOf(purchase(Purchase.PurchaseState.PURCHASED))

        // When
        val result = toPurchaseResult(BillingResponseCode.OK, purchases)

        // Then
        assertEquals(PurchaseResult.SUCCESS, result)
    }

    @Test
    fun `toPurchaseResult() should return PENDING when the premium purchase is pending`() {
        // Given
        val purchases = listOf(purchase(Purchase.PurchaseState.PENDING))

        // When
        val result = toPurchaseResult(BillingResponseCode.OK, purchases)

        // Then
        assertEquals(PurchaseResult.PENDING, result)
    }

    @Test
    fun `toPurchaseResult() should return ERROR when the response is OK without any purchase`() {
        // When
        val result = toPurchaseResult(BillingResponseCode.OK, emptyList())

        // Then
        assertEquals(PurchaseResult.ERROR, result)
    }

    @Test
    fun `toPurchaseResult() should return CANCELED when the user canceled`() {
        // When
        val result = toPurchaseResult(BillingResponseCode.USER_CANCELED, emptyList())

        // Then
        assertEquals(PurchaseResult.CANCELED, result)
    }

    @Test
    fun `toPurchaseResult() should return ALREADY_OWNED when the item is already owned`() {
        // When
        val result = toPurchaseResult(BillingResponseCode.ITEM_ALREADY_OWNED, emptyList())

        // Then
        assertEquals(PurchaseResult.ALREADY_OWNED, result)
    }

    @Test
    fun `toPurchaseResult() should return ERROR for any other response code`() {
        // When
        val results = listOf(
            BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingResponseCode.BILLING_UNAVAILABLE,
            BillingResponseCode.NETWORK_ERROR,
            BillingResponseCode.ERROR
        ).map { toPurchaseResult(it, emptyList()) }

        // Then
        assertEquals(List(4) { PurchaseResult.ERROR }, results)
    }

    private fun purchase(purchaseState: Int): Purchase =
        mock {
            on { products } doReturn listOf(PREMIUM_PRODUCT_ID)
            on { this.purchaseState } doReturn purchaseState
        }
}
