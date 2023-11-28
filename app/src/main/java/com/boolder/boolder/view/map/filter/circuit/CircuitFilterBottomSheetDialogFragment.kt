package com.boolder.boolder.view.map.filter.circuit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.view.compose.BoolderTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CircuitFilterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val args by navArgs<CircuitFilterBottomSheetDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    CircuitFilterLayout(
                        availableCircuits = args.availableCircuits.toList(),
                        onCircuitSelected = ::onCircuitSelected
                    )
                }
            }
        }

    private fun onCircuitSelected(circuit: Circuit?) {
        setFragmentResult(
            requestKey = REQUEST_KEY,
            result = bundleOf(RESULT_CIRCUIT to circuit)
        )
        dismiss()
    }

    companion object {
        const val REQUEST_KEY = "circuit_selection"
        const val RESULT_CIRCUIT = "result_circuit"
    }
}
