package com.boolder.boolder.view.discover.levels.beginner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class BeginnerLevelsFragment : Fragment() {

    private val viewModel by viewModel<BeginnerLevelsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsState()

                    BeginnerLevelsScreen(
                        screenState = screenState,
                        onBackPressed = { findNavController().popBackStack() },
                        onAreaClicked = ::onAreaClicked
                    )
                }
            }
        }

    private fun onAreaClicked(areaId: Int) {
        val direction = BeginnerLevelsFragmentDirections.navigateToAreaOverviewScreen(
            areaId = areaId,
            displayShowOnMapButton = true
        )

        findNavController().navigate(direction)
    }
}
