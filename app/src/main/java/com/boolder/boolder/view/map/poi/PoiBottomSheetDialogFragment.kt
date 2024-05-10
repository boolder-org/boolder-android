package com.boolder.boolder.view.map.poi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.custom.EdgeToEdgeBottomSheetDialogFragment

class PoiBottomSheetDialogFragment : EdgeToEdgeBottomSheetDialogFragment() {

    private val args by navArgs<PoiBottomSheetDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(inflater.context).apply {
            setContent {
                BoolderTheme {
                    PoiLayout(
                        poiName = args.poiName,
                        onOpenPoiInGoogleMaps = { openGoogleMaps(args.googleMapsUrl) },
                        onCloseClicked = { findNavController().popBackStack() }
                    )
                }
            }
        }

    private fun openGoogleMaps(googleMapsUrl: String) {
        val sendIntent = Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsUrl))
        val shareIntent = Intent.createChooser(sendIntent, null)

        try {
            startActivity(shareIntent)
        } catch (e: Exception) {
            Log.i("PoiBottomSheet", "No apps can handle this kind of intent")
        }
    }
}
