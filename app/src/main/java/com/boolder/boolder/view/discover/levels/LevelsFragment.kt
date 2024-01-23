package com.boolder.boolder.view.discover.levels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class LevelsFragment : Fragment() {

    private val viewModel by viewModel<LevelsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                    LevelsScreen(
                        screenState = screenState,
                        onBackPressed = { findNavController().popBackStack() },
                        onMoreBeginnerFriendlyAreasClicked = ::onMoreBeginnerFriendlyAreasClicked,
                        onAreaClicked = ::onAreaClicked
                    )
                }
            }
        }

    private fun onMoreBeginnerFriendlyAreasClicked() {
        val direction = LevelsFragmentDirections.navigateToBeginnerLevelsScreen()

        findNavController().navigate(direction)
    }

    private fun onAreaClicked(areaId: Int) {
        val direction = LevelsFragmentDirections.navigateToAreaOverviewScreen(
            areaId = areaId,
            displayShowOnMapButton = true
        )

        findNavController().navigate(direction)
    }
}
