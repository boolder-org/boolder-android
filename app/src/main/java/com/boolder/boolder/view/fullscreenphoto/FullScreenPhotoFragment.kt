package com.boolder.boolder.view.fullscreenphoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.custom.EdgeToEdgeBottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class FullScreenPhotoFragment : EdgeToEdgeBottomSheetDialogFragment() {

    private val viewModel by viewModel<FullScreenPhotoViewModel>()

    private val scrollOffsetState = mutableFloatStateOf(1f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                val scrollOffset by remember { scrollOffsetState }

                BoolderTheme {
                    FullScreenPhotoScreen(
                        screenState = screenState,
                        scrollOffset = scrollOffset,
                        onCloseClicked = ::onCloseClicked,
                        onDragStateChanged = ::onDragStateChanged
                    )
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED

            addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    scrollOffsetState.floatValue = .5f + .5f * slideOffset
                }
            })
        }
    }

    private fun onCloseClicked() {
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun onDragStateChanged(canDragParentContainer: Boolean) {
        (dialog as? BottomSheetDialog)?.behavior?.isDraggable = canDragParentContainer
    }
}
