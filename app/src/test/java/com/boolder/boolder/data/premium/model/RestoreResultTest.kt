package com.boolder.boolder.data.premium.model

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.Purchase
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class RestoreResultTest {

    @Test
    fun `toRestoreResult() should return RESTORED when the premium product is purchased`() {
        // Given
        val purchases = listOf(purchase(Purchase.PurchaseState.PURCHASED))

        // When
        val result = toRestoreResult(BillingResponseCode.OK, purchases)

        // Then
        assertEquals(RestoreResult.RESTORED, result)
    }

    @Test
    fun `toRestoreResult() should return NOT_FOUND when there is no purchase`() {
        // When
        val result = toRestoreResult(BillingResponseCode.OK, emptyList())

        // Then
        assertEquals(RestoreResult.NOT_FOUND, result)
    }

    @Test
    fun `toRestoreResult() should return NOT_FOUND when the premium purchase is pending`() {
        // Given
        val purchases = listOf(purchase(Purchase.PurchaseState.PENDING))

        // When
        val result = toRestoreResult(BillingResponseCode.OK, purchases)

        // Then
        assertEquals(RestoreResult.NOT_FOUND, result)
    }

    @Test
    fun `toRestoreResult() should return ERROR when Google Play can't be reached`() {
        // When
        val results = listOf(
            BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingResponseCode.BILLING_UNAVAILABLE,
            BillingResponseCode.NETWORK_ERROR,
            BillingResponseCode.ERROR
        ).map { toRestoreResult(it, emptyList()) }

        // Then
        assertEquals(List(4) { RestoreResult.ERROR }, results)
    }

    private fun purchase(purchaseState: Int): Purchase =
        mock {
            on { products } doReturn listOf(PREMIUM_PRODUCT_ID)
            on { this.purchaseState } doReturn purchaseState
        }
}
