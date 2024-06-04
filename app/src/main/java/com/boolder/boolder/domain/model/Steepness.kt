package com.boolder.boolder.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.boolder.boolder.R

enum class Steepness(
    @StringRes val textRes: Int,
    @DrawableRes val iconRes: Int,
    val databaseValue: String
) {
    SLAB(
        textRes = R.string.steepness_slab,
        iconRes = R.drawable.ic_steepness_slab,
        databaseValue = "slab"
    ),
    OVERHANG(
        textRes = R.string.steepness_overhang,
        iconRes = R.drawable.ic_steepness_overhang,
        databaseValue = "overhang"
    ),
    ROOF(
        textRes = R.string.steepness_roof,
        iconRes = R.drawable.ic_steepness_roof,
        databaseValue = "roof"
    ),
    WALL(
        textRes = R.string.steepness_wall,
        iconRes = R.drawable.ic_steepness_wall,
        databaseValue = "wall"
    ),
    TRAVERSE(
        textRes = R.string.steepness_traverse,
        iconRes = R.drawable.ic_steepness_traverse_left_right,
        databaseValue = "traverse"
    );

    companion object {
        fun fromDatabaseValue(value: String): Steepness? =
            Steepness.entries.find { it.databaseValue == value }
    }
}
