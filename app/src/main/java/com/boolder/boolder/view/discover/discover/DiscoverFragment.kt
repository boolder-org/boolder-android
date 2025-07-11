package com.boolder.boolder.view.discover.discover

import android.content.ActivityNotFoundException
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
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.R
import com.boolder.boolder.utils.getLanguage
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class DiscoverFragment : Fragment() {

    private val viewModel by viewModel<DiscoverViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                    val event by viewModel.events.collectAsStateWithLifecycle(null)

                    DiscoverScreen(
                        screenState = screenState,
                        onDiscoverHeaderItemClicked = ::onDiscoverHeaderItemClicked,
                        onAreaClicked = ::onAreaClicked,
                        onRateAppClicked = ::onOpenPlayStorePage,
                        onContributeClicked = ::onOpenContributeWebPage,
                        event = event
                    )
                }
            }
        }

    private fun onDiscoverHeaderItemClicked(item: DiscoverHeaderItem) {
        val navController = findNavController()

        if (navController.currentDestination?.id != R.id.discover_fragment) return

        val direction = when (item) {
            DiscoverHeaderItem.BEGINNER_GUIDE -> {
                openBeginnerGuideArticle()
                return
            }
            DiscoverHeaderItem.PER_LEVEL -> DiscoverFragmentDirections.navigateToLevelsScreen()
            DiscoverHeaderItem.DRIES_FAST -> DiscoverFragmentDirections.navigateToDriesFastScreen()
            DiscoverHeaderItem.TRAIN_AND_BIKE -> DiscoverFragmentDirections.navigateToTrainAndBikeScreen()
        }

        navController.navigate(direction)
    }

    private fun openBeginnerGuideArticle() {
        openWebUrl("https://www.boolder.com/${getLanguage()}/articles/beginners-guide")
    }

    private fun onAreaClicked(areaId: Int) {
        val navController = findNavController()

        if (navController.currentDestination?.id != R.id.discover_fragment) return

        val direction = DiscoverFragmentDirections.navigateToAreaOverviewScreen(
            areaId = areaId,
            displayShowOnMapButton = true
        )

        navController.navigate(direction)
    }

    private fun onOpenPlayStorePage() {
        openWebUrl("https://play.google.com/store/apps/details?id=com.boolder.boolder")
    }

    private fun onOpenContributeWebPage() {
        openWebUrl("https://www.boolder.com/${getLanguage()}/contribute")
    }

    private fun openWebUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            viewModel.onNoBrowserAvailable(url)
        }
    }
}
