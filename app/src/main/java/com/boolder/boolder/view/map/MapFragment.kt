package com.boolder.boolder.view.map

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boolder.boolder.R
import com.boolder.boolder.databinding.FragmentMapBinding
import com.boolder.boolder.viewBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.extension.observable.eventdata.SourceAddedEventData
import com.mapbox.maps.extension.observable.eventdata.StyleLoadedEventData
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.sources.generated.vectorSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.delegates.listeners.OnSourceAddedListener
import com.mapbox.maps.plugin.delegates.listeners.OnStyleLoadedListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar


class MapFragment : Fragment(), LocationCallback {

    private val binding by viewBinding(FragmentMapBinding::bind)

    private lateinit var locationProvider: LocationProvider

    private val styleUri = "mapbox://styles/nmondollot/cl95n147u003k15qry7pvfmq2"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        locationProvider = LocationProvider(requireActivity() as AppCompatActivity, this)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        addSources()
        addLayers()

        binding.fabLocation.setOnClickListener {
            locationProvider.askForPosition()
        }

    }


    override fun onGPSLocation(location: Location) {
        val point = Point.fromLngLat(location.longitude, location.latitude)

        binding.mapView.getMapboxMap()
            .setCamera(CameraOptions.Builder().center(point).bearing(location.bearing.toDouble()).build())
        binding.mapView.gestures.focalPoint = binding.mapView.getMapboxMap().pixelForCoordinate(point)
        binding.mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

    private fun setupMap() {
        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(2.5968216, 48.3925623))
            .zoom(10.2)
            .build()

        binding.mapView.getMapboxMap().apply {
            loadStyleUri(styleUri)
            setCamera(cameraOptions)
        }
        binding.mapView.apply {
            gestures.pitchEnabled = false
            gestures.simultaneousRotateAndPinchToZoomEnabled = false
            scalebar.enabled = false
        }
    }

    private fun addSources() {

        binding.mapView.getMapboxMap().addOnSourceAddedListener(object : OnSourceAddedListener {
            override fun onSourceAdded(eventData: SourceAddedEventData) {
                println("MAPBOX SOURCE ADDED")
            }
        })

        binding.mapView.getMapboxMap().addOnStyleLoadedListener(object : OnStyleLoadedListener {
            override fun onStyleLoaded(eventData: StyleLoadedEventData) {
                println("MAPBOX STYLE LOADED ")
            }
        })

        binding.mapView.getMapboxMap().addOnMapLoadErrorListener(object : OnMapLoadErrorListener {
            override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                println("MAP STYLE SOURCE GOT ERROR ${eventData.message}")
            }
        }
        )

        binding.mapView.getMapboxMap().loadStyle(
            style(styleUri) {
                vectorSource("problems") {
                    url("mapbox://nmondollot.4xsv235p")
                }
            }
        )
    }

    private fun addLayers() {
        val problemsSourceLayerId = "problems-ayes3a" // name of the layer in the mapbox tileset

        binding.mapView.getMapboxMap().loadStyle(
            style(styleUri) {
                circleLayer("problems", "problems") {
                    sourceLayer(problemsSourceLayerId)
                    minZoom(15.0)
                    filter(
                        Expression.match(
                            Expression.geometryType(),
                            Expression.toBoolean(
                                Expression.string()
                            )
                        )
                    )
                    circleRadius(
                        Expression.interpolate(
                            Expression.linear(),
                            Expression.zoom(),
                            Expression.abs(15.0),
                            Expression.abs(2.0),
                            Expression.abs(18.0),
                            Expression.abs(4.0),
                            Expression.abs(22.0),
                            Expression.switchCase(
                                Expression.boolean(
                                    Expression.has("circuitColor"),
                                    Expression.boolean(Expression.string())
                                ),
                                Expression.abs(16.0),
                                Expression.abs(10.0)
                            )
                        )
                    )
                }
            }
        )

    }


}