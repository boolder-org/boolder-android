package com.boolder.boolder.view.map.filter.grade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.domain.model.GradeRange
import com.boolder.boolder.utils.extension.launchAndCollectIn
import com.boolder.boolder.view.compose.BoolderTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class GradesFilterBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val viewModel by viewModel<GradesFilterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                val screenState by viewModel.screenStateFlow.collectAsState()

                BoolderTheme {
                    GradesFilterLayout(
                        gradeRanges = screenState.gradeRanges,
                        selectedGradeRange = screenState.selectedGradeRange,
                        onGradeRangeSelected = viewModel::onGradeRangeSelected,
                        onCustomLowBoundSelected = viewModel::onCustomLowBoundSelected,
                        onCustomHighBoundSelected = viewModel::onCustomHighBoundSelected,
                        onGradeRangeReset = viewModel::onGradeRangeReset,
                        onGradeRangeValidated = viewModel::onGradeRangeValidated
                    )
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.eventFlow.launchAndCollectIn(this) { event ->
            if (event is GradesFilterViewModel.Event.GradesRangeValidated) {
                onGradeRangeValidated(event.gradeRange)
            }
        }
    }

    private fun onGradeRangeValidated(gradeRange: GradeRange) {
        setFragmentResult(
            requestKey = REQUEST_KEY,
            result = bundleOf(RESULT_GRADE_RANGE to gradeRange)
        )
        findNavController().popBackStack()
    }

    companion object {
        const val REQUEST_KEY = "grade_range_selection"
        const val RESULT_GRADE_RANGE = "result_grade_range"
    }
}
