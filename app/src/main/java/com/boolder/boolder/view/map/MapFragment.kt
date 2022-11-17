package com.boolder.boolder.view.map

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.boolder.boolder.R
import com.boolder.boolder.databinding.FragmentMapBinding
import com.boolder.boolder.viewBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraOptions.Builder
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar


class MapFragment : Fragment(), LocationCallback {

    private val binding by viewBinding(FragmentMapBinding::bind)

    private lateinit var locationProvider: LocationProvider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        locationProvider = LocationProvider(requireActivity() as AppCompatActivity, this)

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


        binding.fabLocation.setOnClickListener {
            locationProvider.askForPosition()
        }

    }

    override fun onGPSLocation(location: Location) {
        val point = Point.fromLngLat(location.longitude, location.latitude)

        binding.mapView.getMapboxMap()
            .setCamera(Builder().center(point).bearing(location.bearing.toDouble()).build())
        binding.mapView.gestures.focalPoint = binding.mapView.getMapboxMap().pixelForCoordinate(point)
        binding.mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

}