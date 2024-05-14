package com.boolder.boolder.utils

import org.junit.After
import org.junit.Test
import java.util.Locale
import kotlin.test.assertTrue

class LocaleUtilsTest {

    private val defaultLocale = Locale.getDefault()

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `getLanguage() should return fr`() {
        // Given
        val locales = listOf(
            Locale.FRANCE,
            Locale.CANADA_FRENCH
        )

        // When
        val languageCodes = locales.map {
            Locale.setDefault(it)
            getLanguage()
        }

        // Then
        assertTrue(languageCodes.all { it == "fr" })
    }

    @Test
    fun `getLanguage() should return en`() {
        // Given
        val locales = listOf(
            Locale.UK,
            Locale.US,
            Locale.GERMANY,
            Locale.ITALIAN,
            Locale("nl", "NL"),
            Locale("es", "ES")
        )

        // When
        val languageCodes = locales.map {
            Locale.setDefault(it)
            getLanguage()
        }

        // Then
        assertTrue(languageCodes.all { it == "en" })
    }
}
