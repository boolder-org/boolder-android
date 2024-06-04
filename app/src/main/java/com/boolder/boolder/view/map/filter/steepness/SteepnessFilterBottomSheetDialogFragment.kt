package com.boolder.boolder.view.map.filter.steepness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.custom.EdgeToEdgeBottomSheetDialogFragment
import com.boolder.boolder.view.map.MapFragment

class SteepnessFilterBottomSheetDialogFragment : EdgeToEdgeBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    SteepnessFilterLayout(
                        onSteepnessClicked = ::onSteepnessClicked,
                        onSteepnessReset = { onSteepnessClicked(-1) }
                    )
                }
            }
        }

    private fun onSteepnessClicked(steepnessIndex: Int) {
        setFragmentResult(
            requestKey = MapFragment.FILTER_REQUEST,
            result = bundleOf(RESULT_STEEPNESS to steepnessIndex)
        )
        findNavController().popBackStack()
    }

    companion object {
        const val RESULT_STEEPNESS = "steepness_filter_result"
    }
}
