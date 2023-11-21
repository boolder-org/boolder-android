package com.boolder.boolder.view.discover.discover

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.boolder.boolder.R
import kotlin.math.roundToInt

enum class DiscoverHeaderItem(
    @StringRes val textRes: Int,
    @ColorInt val backgroundStartColor: Int,
    @ColorInt val backgroundEndColor: Int,
    @DrawableRes val iconRes: Int? = null
) {
    BEGINNER_GUIDE(
        textRes = R.string.discover_header_item_beginner_guide,
        backgroundStartColor = iosGreen(.4f),
        backgroundEndColor = iosGreen(.6f)
    ),
    PER_LEVEL(
        textRes = R.string.discover_header_item_per_level,
        backgroundStartColor = iosBlue(.4f),
        backgroundEndColor = iosBlue(.6f),
        iconRes = R.drawable.ic_signal_cellular_alt
    ),
    DRIES_FAST(
        textRes = R.string.discover_header_item_dries_fast,
        backgroundStartColor = iosYellow(.4f),
        backgroundEndColor = iosYellow(.6f),
        iconRes = R.drawable.ic_light_mode
    ),
    TRAIN_AND_BIKE(
        textRes = R.string.discover_header_item_train_and_bike,
        backgroundStartColor = iosRed(.4f),
        backgroundEndColor = iosRed(.6f)
    )
}

private fun iosRed(alpha: Float) = Color.argb((alpha * 255f).roundToInt(), 255, 59, 48)
private fun iosGreen(alpha: Float) = Color.argb((alpha * 255f).roundToInt(), 52, 199, 89)
private fun iosBlue(alpha: Float) = Color.argb((alpha * 255f).roundToInt(), 0, 122, 255)
private fun iosYellow(alpha: Float) = Color.argb((alpha * 255f).roundToInt(), 255, 204, 0)
