package com.boolder.boolder.view.ticklist.compose

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

class TopBarActionTooltipPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        var x = anchorBounds.left + anchorBounds.width / 2 - popupContentSize.width / 2

        val popupContentEnd = x + popupContentSize.width

        if (popupContentEnd > windowSize.width) {
            x -= popupContentEnd - windowSize.width
        }

        return IntOffset(x = x, y = anchorBounds.bottom)
    }
}
