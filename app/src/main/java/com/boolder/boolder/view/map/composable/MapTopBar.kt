package com.boolder.boolder.view.map.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.boolder.boolder.databinding.SearchComponentBinding
import com.boolder.boolder.utils.previewgenerator.dummyOfflineAreaItem
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem

@Composable
fun MapTopBar(
    offlineAreaItem: OfflineAreaItem?,
    onHideAreaName: () -> Unit,
    onAreaInfoClicked: () -> Unit,
    onSearchBarClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        AndroidViewBinding(
            factory = { inflater, parent, attachToParent ->
                SearchComponentBinding.inflate(inflater, parent, attachToParent).also {
                    it.searchBar.isFocusable = false
                    it.searchBar.isClickable = false
                    it.searchBar.isLongClickable = false
                    it.searchBar.setTextIsSelectable(false)
                    it.searchBar.setOnClickListener { onSearchBarClicked() }
                }
            }
        )

        AreaName(
            offlineAreaItem = offlineAreaItem,
            onHideAreaName = onHideAreaName,
            onAreaInfoClicked = onAreaInfoClicked
        )
    }
}

@Preview
@Composable
private fun MapTopBarPreview(
    @PreviewParameter(MapTopBarParameterPreviewProvider::class)
    offlineAreaItem: OfflineAreaItem?
) {
    BoolderTheme {
        MapTopBar(
            offlineAreaItem = offlineAreaItem,
            onHideAreaName = {},
            onAreaInfoClicked = {},
            onSearchBarClicked = {}
        )
    }
}

private class MapTopBarParameterPreviewProvider : PreviewParameterProvider<OfflineAreaItem?> {
    override val values = sequenceOf(null, dummyOfflineAreaItem())
}
