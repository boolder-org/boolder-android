package com.boolder.boolder.view.areadetails.areacircuit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.view.areadetails.KEY_CIRCUIT_ID
import com.boolder.boolder.view.areadetails.KEY_PROBLEM
import com.boolder.boolder.view.areadetails.REQUEST_KEY_AREA_DETAILS
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AreaCircuitFragment : Fragment() {

    private val args by navArgs<AreaCircuitFragmentArgs>()
    private val viewModel by viewModel<AreaCircuitViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                BoolderTheme {
                    AreaCircuitScreen(
                        screenState = screenState,
                        onBackPressed = { findNavController().popBackStack() },
                        onProblemClicked = ::onProblemClicked,
                        onSeeOnMapClicked = ::onSeeOnMapClicked
                    )
                }
            }
        }

    private fun onProblemClicked(problem: Problem) {
        setFragmentResult(
            REQUEST_KEY_AREA_DETAILS,
            bundleOf(KEY_PROBLEM to problem)
        )

        findNavController().popBackStack(R.id.map_fragment, inclusive = false)
    }

    private fun onSeeOnMapClicked() {
        setFragmentResult(
            REQUEST_KEY_AREA_DETAILS,
            bundleOf(KEY_CIRCUIT_ID to args.circuitId)
        )

        findNavController().popBackStack(R.id.map_fragment, inclusive = false)
    }
}
