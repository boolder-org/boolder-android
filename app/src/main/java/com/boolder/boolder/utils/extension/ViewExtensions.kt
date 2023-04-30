package com.boolder.boolder.utils.extension

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.setOnApplyWindowTopInsetListener(block: (topInset: Int) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top

        block(topInset)
        insets
    }
}
