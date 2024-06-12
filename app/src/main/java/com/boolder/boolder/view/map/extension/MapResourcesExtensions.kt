package com.boolder.boolder.view.map.extension

import android.content.res.Resources

private const val HEIGHT_DP_AREA_BAR = 50
private const val HEIGHT_DP_FILTERS_BAR = 48
private const val HEIGHT_DP_TOPO_BOTTOM_SHEET = 354
private const val HEIGHT_DP_CIRCUIT_START_BUTTON = 48
private const val HEIGHT_DP_MARGIN_16 = 16

fun Resources.getAreaBarAndFiltersHeight(): Float =
    displayMetrics.density * (HEIGHT_DP_MARGIN_16 + HEIGHT_DP_AREA_BAR + HEIGHT_DP_MARGIN_16 + HEIGHT_DP_FILTERS_BAR + HEIGHT_DP_MARGIN_16)

fun Resources.getAreaBarHeight(): Float =
    displayMetrics.density * (HEIGHT_DP_MARGIN_16 + HEIGHT_DP_AREA_BAR)

fun Resources.getTopoBottomSheetHeight(): Float =
    displayMetrics.density * HEIGHT_DP_TOPO_BOTTOM_SHEET

fun Resources.getTopoBottomSheetHeightWithMargin(): Float =
    displayMetrics.density * (HEIGHT_DP_TOPO_BOTTOM_SHEET + HEIGHT_DP_MARGIN_16)

fun Resources.getCircuitStartButtonHeight(): Float =
    displayMetrics.density * (HEIGHT_DP_MARGIN_16 + HEIGHT_DP_CIRCUIT_START_BUTTON + HEIGHT_DP_MARGIN_16)

fun Resources.getCircuitHorizontalMargin(): Float =
    displayMetrics.density * HEIGHT_DP_MARGIN_16 * 4

fun Resources.getDefaultMargin(): Float =
    displayMetrics.density * HEIGHT_DP_MARGIN_16
