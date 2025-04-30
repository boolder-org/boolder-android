package com.boolder.boolder.view.discover.driesfast

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

class DriesFastFragment : Fragment() {

    private val viewModel by viewModel<DriesFastViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                    DriesFastScreen(
                        screenState = screenState,
                        onBackPressed = { findNavController().popBackStack() },
                        onAreaClicked = ::onAreaClicked
                    )
                }
            }
        }

    private fun onAreaClicked(areaId: Int) {
        val direction = DriesFastFragmentDirections.navigateToAreaOverviewScreen(
            areaId = areaId,
            displayShowOnMapButton = true
        )

        findNavController().navigate(direction)
    }
}
