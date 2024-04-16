package com.boolder.boolder.view.areadetails.areaproblems

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
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.view.areadetails.KEY_PROBLEM
import com.boolder.boolder.view.areadetails.REQUEST_KEY_AREA_DETAILS
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AreaProblemsFragment : Fragment() {

    private val viewModel by viewModel<AreaProblemsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                BoolderTheme {
                    AreaProblemsScreen(
                        screenState = screenState,
                        onBackPressed = { findNavController().popBackStack() },
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onProblemClicked = ::onProblemClicked
                    )
                }
            }
        }

    private fun onProblemClicked(problem: Problem) {
        setFragmentResult(
            requestKey = REQUEST_KEY_AREA_DETAILS,
            result = bundleOf(KEY_PROBLEM to problem)
        )

        findNavController().navigate(resId = R.id.map_fragment)
    }
}
