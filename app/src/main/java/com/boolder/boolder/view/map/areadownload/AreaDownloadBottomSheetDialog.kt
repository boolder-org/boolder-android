package com.boolder.boolder.view.map.areadownload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boolder.boolder.view.compose.BoolderRippleTheme
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.custom.EdgeToEdgeBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class AreaDownloadBottomSheetDialog : EdgeToEdgeBottomSheetDialogFragment() {

    private val viewModel by viewModel<AreaDownloadViewModel>()

    private val scrollOffsetState = mutableFloatStateOf(0f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                    val scrollOffset by remember { scrollOffsetState }

                    CompositionLocalProvider(LocalRippleTheme provides BoolderRippleTheme) {
                        AreaDownloadLayout(
                            modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection()),
                            screenState = screenState,
                            offlineClusterDownloader = viewModel,
                            offlineAreaDownloader = viewModel,
                            scrollOffset = scrollOffset,
                            onDismissHint = { viewModel.onDismissDownloadHint() }
                        )
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)
            ?.behavior
            ?.addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    scrollOffsetState.floatValue = slideOffset.coerceAtLeast(0f)
                }
            })
    }
}
