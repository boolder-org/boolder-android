package com.boolder.boolder.data.premium.model

import com.android.billingclient.api.Purchase
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun `isPhotoLocked() should return false for a free area when not subscribed`() {
        // When
        val isLocked = isPhotoLocked(areaId = 1, freeAreaIds = setOf(1, 2), status = PremiumStatus.NOT_SUBSCRIBED)

        // Then
        assertFalse(isLocked)
    }

    @Test
    fun `isPhotoLocked() should return true for a paid area when not subscribed`() {
        // When
        val isLocked = isPhotoLocked(areaId = 3, freeAreaIds = setOf(1, 2), status = PremiumStatus.NOT_SUBSCRIBED)

        // Then
        assertTrue(isLocked)
    }

    @Test
    fun `isPhotoLocked() should return false for a paid area when subscribed`() {
        // When
        val isLocked = isPhotoLocked(areaId = 3, freeAreaIds = setOf(1, 2), status = PremiumStatus.SUBSCRIBED)

        // Then
        assertFalse(isLocked)
    }

    @Test
    fun `isPhotoLocked() should return true for any area when free areas are not loaded and not subscribed`() {
        // When
        val isLocked = isPhotoLocked(areaId = 1, freeAreaIds = emptySet(), status = PremiumStatus.NOT_SUBSCRIBED)

        // Then
        assertTrue(isLocked)
    }

    @Test
    fun `isPhotoLocked() should return false for any area when free areas are not loaded and subscribed`() {
        // When
        val isLocked = isPhotoLocked(areaId = 1, freeAreaIds = emptySet(), status = PremiumStatus.SUBSCRIBED)

        // Then
        assertFalse(isLocked)
    }

    private fun purchase(productId: String, purchaseState: Int): Purchase =
        mock {
            on { products } doReturn listOf(productId)
            on { this.purchaseState } doReturn purchaseState
        }
}
