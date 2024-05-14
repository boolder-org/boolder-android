package com.boolder.boolder.view.fullscreenphoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.view.compose.BoolderTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class FullScreenPhotoFragment : Fragment() {

    private val viewModel by viewModel<FullScreenPhotoViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                BoolderTheme {
                    FullScreenPhotoScreen(
                        screenState = screenState,
                        onCloseClicked = { findNavController().popBackStack() }
                    )
                }
            }
        }
}
