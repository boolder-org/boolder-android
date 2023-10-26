package com.boolder.boolder.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.boolder.boolder.R

enum class Steepness(
    @StringRes val textRes: Int,
    @DrawableRes val iconRes: Int
) {
    SLAB(
        textRes = R.string.stepness_slab,
        iconRes = R.drawable.ic_steepness_slab
    ),
    OVERHANG(
        textRes = R.string.stepness_overhang,
        iconRes = R.drawable.ic_steepness_overhang
    ),
    ROOF(
        textRes = R.string.stepness_roof,
        iconRes = R.drawable.ic_steepness_roof
    ),
    WALL(
        textRes = R.string.stepness_wall,
        iconRes = R.drawable.ic_steepness_wall
    ),
    TRAVERSE(
        textRes = R.string.stepness_traverse,
        iconRes = R.drawable.ic_steepness_traverse_left_right
    );

    companion object {
        fun fromTextValue(value: String): Steepness? = when (value.lowercase()) {
            "slab" -> SLAB
            "overhang" -> OVERHANG
            "roof" -> ROOF
            "wall" -> WALL
            "traverse" -> TRAVERSE
            else -> null
        }
    }
}
