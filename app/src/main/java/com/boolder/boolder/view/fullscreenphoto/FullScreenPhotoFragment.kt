package com.boolder.boolder.view.fullscreenphoto

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
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
                        onDragStateChanged = ::onDragStateChanged,
                        onTopoSwiped = viewModel::onTopoSwiped,
                        onProblemSelected = viewModel::onProblemSelected
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

    // the problem details bottom sheet shows the picture that was last seen in full screen
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        // also called when the dialog is recreated on a configuration change
        if (activity?.isChangingConfigurations == true) return

        val shownTopo = viewModel.getChangedTopo() ?: return

        val problemId = shownTopo.selectedCompleteProblem?.problemWithLine?.problem?.id ?: return

        setFragmentResult(
            requestKey = REQUEST_KEY,
            result = bundleOf(
                RESULT_TOPO_ID to shownTopo.topoId,
                RESULT_PROBLEM_ID to problemId,
                RESULT_IS_TOPO_MODE to shownTopo.isTopoMode
            )
        )
    }

    companion object {
        const val REQUEST_KEY = "full_screen_photo"
        const val RESULT_TOPO_ID = "result_topo_id"
        const val RESULT_PROBLEM_ID = "result_problem_id"
        const val RESULT_IS_TOPO_MODE = "result_is_topo_mode"
    }
}
