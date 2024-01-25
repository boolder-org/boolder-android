package com.boolder.boolder.view.custom

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class EdgeToEdgeBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        object : BottomSheetDialog(requireContext(), theme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()

                window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }

                findViewById<View>(com.google.android.material.R.id.container)
                    ?.fitsSystemWindows = false

                findViewById<View>(com.google.android.material.R.id.coordinator)
                    ?.fitsSystemWindows = false
            }
        }
}
