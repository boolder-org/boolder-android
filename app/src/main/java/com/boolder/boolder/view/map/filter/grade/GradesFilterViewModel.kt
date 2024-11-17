package com.boolder.boolder.view.map.filter.grade

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.datastore.PREF_CUSTOM_GRADE_RANGE_MAX
import com.boolder.boolder.data.datastore.PREF_CUSTOM_GRADE_RANGE_MIN
import com.boolder.boolder.domain.model.ALL_GRADES
import com.boolder.boolder.domain.model.GradeRange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GradesFilterViewModel(
    savedStateHandle: SavedStateHandle,
    private val preferencesDatastore: DataStore<Preferences>
) : ViewModel() {

    private val customGradeRangeFlow = preferencesDatastore.data
        .map {
            val minGrade = it[PREF_CUSTOM_GRADE_RANGE_MIN]
            val maxGrade = it[PREF_CUSTOM_GRADE_RANGE_MAX]

            Log.d("WANG", "New values: $minGrade, $maxGrade")

            if (minGrade == null || maxGrade == null) return@map DEFAULT_CUSTOM_RANGE

            GradeRange(min = minGrade, max = maxGrade)
        }
        .onEach { customGradeRange ->
            _screenStateFlow.update { it.copy(gradeRanges = QUICK_GRADE_RANGES + customGradeRange) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = savedStateHandle.get<GradeRange>(ARG_GRADE_RANGE)
                ?.takeIf {it.isCustom }
                ?: DEFAULT_CUSTOM_RANGE
        )

    private val _screenStateFlow = MutableStateFlow(
        ScreenState(
            gradeRanges = QUICK_GRADE_RANGES,
            selectedGradeRange = requireNotNull(savedStateHandle.get<GradeRange>(ARG_GRADE_RANGE))
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
        val minGradeIndex = ALL_GRADES.indexOf(grade)
        val maxGradeIndex = ALL_GRADES.indexOf(customGradeRangeFlow.value.max)

        val customGradeRange = GradeRange(
            min = grade,
            max = if (minGradeIndex > maxGradeIndex) grade else customGradeRangeFlow.value.max
        )

        updateCustomGradeRange(customGradeRange)
    }

    fun onCustomHighBoundSelected(grade: String) {
        val minGradeIndex = ALL_GRADES.indexOf(customGradeRangeFlow.value.min)
        val maxGradeIndex = ALL_GRADES.indexOf(grade)

        val customGradeRange = GradeRange(
            min = if (minGradeIndex > maxGradeIndex) grade else customGradeRangeFlow.value.min,
            max = grade
        )

        updateCustomGradeRange(customGradeRange)
    }

    private fun updateCustomGradeRange(gradeRange: GradeRange) {
        viewModelScope.launch {
            preferencesDatastore.edit {
                it[PREF_CUSTOM_GRADE_RANGE_MIN] = gradeRange.min
                it[PREF_CUSTOM_GRADE_RANGE_MAX] = gradeRange.max
            }

            _screenStateFlow.update { it.copy(selectedGradeRange = gradeRange) }
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
        private const val ARG_GRADE_RANGE = "grade_range"

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
