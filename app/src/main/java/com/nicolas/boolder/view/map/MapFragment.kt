package com.nicolas.boolder.view.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar
import com.nicolas.boolder.R
import com.nicolas.boolder.databinding.FragmentMapBinding
import com.nicolas.boolder.viewBinding

class MapFragment : Fragment() {

    private val binding by viewBinding(FragmentMapBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(2.5968216, 48.3925623))
            .zoom(10.2)
            .build()

        binding.mapView.getMapboxMap().apply {
            loadStyleUri("mapbox://styles/nmondollot/cl95n147u003k15qry7pvfmq2")
            setCamera(cameraOptions)
        }
        binding.mapView.apply {
            gestures.pitchEnabled = false
            gestures.simultaneousRotateAndPinchToZoomEnabled = false
            scalebar.enabled = false
        }
    }
}