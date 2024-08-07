package com.boolder.boolder.view.offlinephotos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class OfflinePhotosFragment : Fragment() {

    private val viewModel by viewModel<OfflinePhotosViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                    OfflinePhotosScreen(
                        screenState = screenState,
                        offlineAreaDownloader = viewModel
                    )
                }
            }
        }
}
