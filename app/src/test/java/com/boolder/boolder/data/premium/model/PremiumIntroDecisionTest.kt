package com.boolder.boolder.data.premium.model

import org.junit.Test
import kotlin.test.assertEquals

class PremiumIntroDecisionTest {

    @Test
    fun `decidePremiumIntro() should return SHOW for an existing user who is not subscribed`() {
        // When
        val decision = decide(isIntroSeen = false, status = PremiumStatus.NOT_SUBSCRIBED, isUpdate = true)

        // Then
        assertEquals(PremiumIntroDecision.SHOW, decision)
    }

    @Test
    fun `decidePremiumIntro() should return NOTHING when the intro has already been seen`() {
        // When
        val decisions = listOf(PremiumStatus.NOT_SUBSCRIBED, PremiumStatus.SUBSCRIBED, null)
            .flatMap { status ->
                listOf(true, false).map { isUpdate -> decide(isIntroSeen = true, status = status, isUpdate = isUpdate) }
            }

        // Then
        assertEquals(List(6) { PremiumIntroDecision.NOTHING }, decisions)
    }

    @Test
    fun `decidePremiumIntro() should return NOTHING when the status is not resolved`() {
        // When
        val decisions = listOf(true, false).map { isUpdate ->
            decide(isIntroSeen = false, status = null, isUpdate = isUpdate)
        }

        // Then
        assertEquals(List(2) { PremiumIntroDecision.NOTHING }, decisions)
    }

    @Test
    fun `decidePremiumIntro() should return MARK_AS_SEEN when the user is already subscribed`() {
        // When
        val decisions = listOf(true, false).map { isUpdate ->
            decide(isIntroSeen = false, status = PremiumStatus.SUBSCRIBED, isUpdate = isUpdate)
        }

        // Then
        assertEquals(List(2) { PremiumIntroDecision.MARK_AS_SEEN }, decisions)
    }

    @Test
    fun `decidePremiumIntro() should return MARK_AS_SEEN for a new user`() {
        // When
        val decision = decide(isIntroSeen = false, status = PremiumStatus.NOT_SUBSCRIBED, isUpdate = false)

        // Then
        assertEquals(PremiumIntroDecision.MARK_AS_SEEN, decision)
    }

    private fun decide(isIntroSeen: Boolean, status: PremiumStatus?, isUpdate: Boolean): PremiumIntroDecision =
        decidePremiumIntro(
            isIntroSeen = isIntroSeen,
            status = status,
            firstInstallTime = INSTALL_TIME,
            lastUpdateTime = if (isUpdate) INSTALL_TIME + 1_000L else INSTALL_TIME
        )

    private companion object {
        const val INSTALL_TIME = 1_700_000_000_000L
    }
}
