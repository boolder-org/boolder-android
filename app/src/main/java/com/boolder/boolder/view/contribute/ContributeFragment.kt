package com.boolder.boolder.view.contribute

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.boolder.boolder.utils.getLanguage
import com.boolder.boolder.view.compose.BoolderTheme

class ContributeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    ContributeScreen(
                        onStartContributingClicked = ::onStartContributingClicked,
                        onLearnMoreClicked = ::onLearnMoreClicked
                    )
                }
            }
        }

    private fun onStartContributingClicked() {
        openWebUrl("https://www.boolder.com/${getLanguage()}/contribute?dismiss_banner=true")
    }

    private fun onLearnMoreClicked() {
        openWebUrl("https://www.boolder.com/${getLanguage()}/about")
    }

    private fun openWebUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())

        startActivity(intent)
    }
}
