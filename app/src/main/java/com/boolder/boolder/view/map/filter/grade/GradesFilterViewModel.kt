package com.boolder.boolder.view.map.filter.grade

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.domain.model.GradeRange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GradesFilterViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var customGradeRange = savedStateHandle.get<GradeRange>(ARG_GRADE_RANGE)
        ?.takeIf {it.isCustom }
        ?: DEFAULT_CUSTOM_RANGE

    private val _screenStateFlow = MutableStateFlow(
        ScreenState(
            gradeRanges = QUICK_GRADE_RANGES + customGradeRange,
            selectedGradeRange = requireNotNull(savedStateHandle[ARG_GRADE_RANGE])
        )
    )
    val screenStateFlow = _screenStateFlow.asStateFlow()

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onGradeRangeSelected(gradeRange: GradeRange) {
        _screenStateFlow.update {
            it.copy(selectedGradeRange = gradeRange)
        }
    }

    fun onCustomLowBoundSelected(grade: String) {
        customGradeRange = customGradeRange.copy(min = grade)
        _screenStateFlow.update {
            it.copy(
                gradeRanges = QUICK_GRADE_RANGES + customGradeRange,
                selectedGradeRange = customGradeRange
            )
        }
    }

    fun onCustomHighBoundSelected(grade: String) {
        customGradeRange = customGradeRange.copy(max = grade)
        _screenStateFlow.update {
            it.copy(
                gradeRanges = QUICK_GRADE_RANGES + customGradeRange,
                selectedGradeRange = customGradeRange
            )
        }
    }

    fun onGradeRangeReset() {
        viewModelScope.launch {
            _eventFlow.emit(Event.GradesRangeValidated(GradeRange.LARGEST))
        }
    }

    fun onGradeRangeValidated() {
        val selectedGradeRange = _screenStateFlow.value.selectedGradeRange

        viewModelScope.launch {
            _eventFlow.emit(Event.GradesRangeValidated(selectedGradeRange))
        }
    }

    data class ScreenState(
        val gradeRanges: List<GradeRange>,
        val selectedGradeRange: GradeRange
    )

    sealed interface Event {
        data class GradesRangeValidated(val gradeRange: GradeRange) : Event
    }

    companion object {
        private const val ARG_GRADE_RANGE = "arg_grade_range"

        private val DEFAULT_CUSTOM_RANGE = GradeRange(min = "4a", max = "5a")

        val QUICK_GRADE_RANGES = listOf(
            GradeRange.BEGINNER,
            GradeRange.LEVEL4,
            GradeRange.LEVEL5,
            GradeRange.LEVEL6,
            GradeRange.LEVEL7
        )
    }
}
