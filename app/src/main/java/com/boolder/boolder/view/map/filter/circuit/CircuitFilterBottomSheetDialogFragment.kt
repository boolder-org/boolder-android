package com.boolder.boolder.view.map.filter.circuit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.view.compose.BoolderTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CircuitFilterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var availableCircuits: List<Circuit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        availableCircuits =
            requireNotNull(arguments?.getParcelableArrayList(ARG_AVAILABLE_CIRCUITS))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    CircuitFilterLayout(
                        availableCircuits = availableCircuits,
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
        private const val ARG_AVAILABLE_CIRCUITS = "arg_available_circuits"

        const val REQUEST_KEY = "circuit_selection"
        const val RESULT_CIRCUIT = "result_circuit"
        const val TAG = "CircuitFilterBottomSheetDialogFragment"

        fun newInstance(availableCircuits: List<Circuit>) =
            CircuitFilterBottomSheetDialogFragment().apply {
                arguments = bundleOf(ARG_AVAILABLE_CIRCUITS to ArrayList(availableCircuits))
            }
    }
}
