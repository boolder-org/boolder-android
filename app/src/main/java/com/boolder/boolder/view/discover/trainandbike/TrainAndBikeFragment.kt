package com.boolder.boolder.view.discover.trainandbike

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
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrainAndBikeFragment : Fragment() {

    private val viewModel by viewModel<TrainAndBikeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                    TrainAndBikeScreen(
                        screenState = screenState,
                        onBackPressed = { findNavController().popBackStack() },
                        onOpenGoogleMapsUrl = ::onOpenGoogleMapsUrl,
                        onAreaClicked = ::onAreaClicked
                    )
                }
            }
        }

    private fun onOpenGoogleMapsUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())

        startActivity(intent)
    }

    private fun onAreaClicked(areaId: Int) {
        val direction = TrainAndBikeFragmentDirections.navigateToAreaOverviewScreen(
            areaId = areaId,
            displayShowOnMapButton = true
        )

        findNavController().navigate(direction)
    }
}
