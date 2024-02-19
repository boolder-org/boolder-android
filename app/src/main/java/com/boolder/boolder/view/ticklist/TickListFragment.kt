package com.boolder.boolder.view.ticklist

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
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.view.areadetails.KEY_PROBLEM
import com.boolder.boolder.view.areadetails.REQUEST_KEY_AREA_DETAILS
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class TickListFragment : Fragment() {

    private val viewModel by viewModel<TickListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                    TickListScreen(
                        screenState = screenState,
                        onProblemClicked = ::onProblemClicked
                    )
                }
            }
        }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState()
    }

    private fun onProblemClicked(problem: Problem) {
        setFragmentResult(
            REQUEST_KEY_AREA_DETAILS,
            bundleOf(KEY_PROBLEM to problem)
        )

        findNavController().popBackStack(R.id.map_fragment, inclusive = false)
    }
}
