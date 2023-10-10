package com.boolder.boolder.view.detail.uimodel

import androidx.annotation.ColorRes

/**
 * Container for the data that is only needed in the UI, in order to render the
 * starting points of the boulder problems.
 *
 * @param x the horizontal coordinate on the photo, in pixels
 * @param y the vertical coordinate on the photo, in pixels
 * @param dpSize the size of the boulder problem start marker, in dp
 * @param colorRes the resource identifying the color of the boulder problem
 * start marker
 * @param textColorRes the resource identifying the text color of the boulder
 * problem start marker
 */
data class ProblemStart(
    val x: Int,
    val y: Int,
    val dpSize: Int,
    @ColorRes val colorRes: Int,
    @ColorRes val textColorRes: Int
)
