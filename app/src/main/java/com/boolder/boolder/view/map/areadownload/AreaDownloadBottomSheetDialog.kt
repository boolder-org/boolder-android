package com.boolder.boolder.view.map.areadownload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.custom.EdgeToEdgeBottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class AreaDownloadBottomSheetDialog : EdgeToEdgeBottomSheetDialogFragment() {

    private val viewModel by viewModel<AreaDownloadViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                    AreaDownloadLayout(
                        screenState = screenState,
                        offlineAreaDownloader = viewModel
                    )
                }
            }
        }
}
