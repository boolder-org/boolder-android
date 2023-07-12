package com.boolder.boolder.domain.model

import android.content.res.Resources
import android.os.Parcelable
import com.boolder.boolder.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class GradeRange(
    val min: String,
    val max: String,
    val isCustom: Boolean = true
) : Parcelable {

    companion object {
        val BEGINNER = GradeRange(min = "1a", max = "3c+", isCustom = false)
        val LEVEL1 = GradeRange(min = "1a", max = "1c+", isCustom = false)
        val LEVEL2 = GradeRange(min = "2a", max = "2c+", isCustom = false)
        val LEVEL3 = GradeRange(min = "3a", max = "3c+", isCustom = false)
        val LEVEL4 = GradeRange(min = "4a", max = "4c+", isCustom = false)
        val LEVEL5 = GradeRange(min = "5a", max = "5c+", isCustom = false)
        val LEVEL6 = GradeRange(min = "6a", max = "6c+", isCustom = false)
        val LEVEL7 = GradeRange(min = "7a", max = "7c+", isCustom = false)
        val LEVEL8 = GradeRange(min = "8a", max = "8c+", isCustom = false)
        val LARGEST = GradeRange(min = ALL_GRADES.first(), ALL_GRADES.last(), isCustom = false)
    }
}

fun Resources.gradeRangeLevelDisplay(gradeRange: GradeRange): String =
    when (gradeRange) {
        GradeRange.BEGINNER -> "${getString(R.string.grade)} 1 ▸ 3"
        GradeRange.LEVEL1 -> "${getString(R.string.grade)} 1"
        GradeRange.LEVEL2 -> "${getString(R.string.grade)} 2"
        GradeRange.LEVEL3 -> "${getString(R.string.grade)} 3"
        GradeRange.LEVEL4 -> "${getString(R.string.grade)} 4"
        GradeRange.LEVEL5 -> "${getString(R.string.grade)} 5"
        GradeRange.LEVEL6 -> "${getString(R.string.grade)} 6"
        GradeRange.LEVEL7 -> "${getString(R.string.grade)} 7"
        GradeRange.LEVEL8 -> "${getString(R.string.grade)} 8"
        else -> "${gradeRange.min} ▸ ${gradeRange.max}"
    }
