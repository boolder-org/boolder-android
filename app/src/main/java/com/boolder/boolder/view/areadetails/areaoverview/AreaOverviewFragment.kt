package com.boolder.boolder.view.areadetails.areaoverview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AreaOverviewFragment : Fragment() {

    private val args by navArgs<AreaOverviewFragmentArgs>()
    private val viewModel by viewModel<AreaOverviewViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                BoolderTheme {
                    AreaOverviewScreen(
                        screenState = screenState,
                        offlineAreaDownloader = viewModel,
                        onBackPressed = { findNavController().popBackStack() },
                        onAreaProblemsCountClicked = ::onAreaProblemsCountClicked,
                        onCircuitClicked = ::onCircuitClicked,
                        onPoiClicked = ::onPoiClicked
                    )
                }
            }
        }

    private fun onAreaProblemsCountClicked() {
        val direction = AreaOverviewFragmentDirections.navigateToAreaProblemsScreen(areaId = args.areaId)

        findNavController().navigate(direction)
    }

    private fun onCircuitClicked(circuitId: Int) {
        val direction = AreaOverviewFragmentDirections.navigateToAreaCircuitScreen(circuitId = circuitId)

        findNavController().navigate(direction)
    }

    private fun onPoiClicked(googleMapsUrl: String) {
        val packageManager = activity?.packageManager ?: return

        val intent = Intent(Intent.ACTION_VIEW, googleMapsUrl.toUri())

        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
    }
}
