package com.boolder.boolder.view.map.filter.steepness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.utils.extension.launchAndCollectIn
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.custom.EdgeToEdgeBottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SteepnessFilterBottomSheetDialogFragment : EdgeToEdgeBottomSheetDialogFragment() {

    private val viewModel by viewModel<SteepnessFilterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                val screenState by viewModel.screenStateFlow.collectAsState()

                BoolderTheme {
                    SteepnessFilterLayout(
                        selectedSteepnesses = screenState.selectedSteepnesses,
                        onSteepnessToggled = viewModel::onSteepnessToggled,
                        onSteepnessesReset = viewModel::onSteepnessesReset,
                        onSteepnessesValidated = viewModel::onSteepnessesValidated
                    )
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.eventFlow.launchAndCollectIn(this) { event ->
            if (event is SteepnessFilterViewModel.Event.SteepnessesValidated) {
                onSteepnessesValidated(event.steepnesses)
            }
        }
    }

    private fun onSteepnessesValidated(steepnesses: List<String>) {
        setFragmentResult(
            requestKey = REQUEST_KEY,
            result = bundleOf(RESULT_STEEPNESSES to steepnesses.toTypedArray())
        )
        findNavController().popBackStack()
    }

    companion object {
        const val REQUEST_KEY = "steepness_selection"
        const val RESULT_STEEPNESSES = "result_steepnesses"
    }
}
