package com.boolder.boolder.utils

import java.util.Locale

fun getLanguage(): String =
    when (Locale.getDefault().language) {
        "fr" -> "fr"
        else -> "en"
    }
