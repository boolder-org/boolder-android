package com.boolder.boolder.view.map

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.boolder.boolder.*
import com.boolder.boolder.databinding.ActivityMainBinding
import com.boolder.boolder.utils.*
import com.boolder.boolder.view.detail.ProblemBSFragment
import com.boolder.boolder.view.search.SearchActivity
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapActivity : AppCompatActivity(), LocationCallback {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val mapViewModel by viewModel<MapViewModel>()
    private val layerFactory: MapboxStyleFactory by inject()

    private lateinit var locationProvider: LocationProvider

    private var shouldClick = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        locationProvider = LocationProvider(this, this)

        setupMap()

        binding.fabLocation.setOnClickListener {
            locationProvider.askForPosition()
        }

        binding.searchComponent.searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val intent = Intent(this, SearchActivity::class.java)
                val option: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(intent, option.toBundle())
            }
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
            setCamera(cameraOptions)
        }
        binding.mapView.apply {
            gestures.pitchEnabled = false
            gestures.simultaneousRotateAndPinchToZoomEnabled = false
            scalebar.enabled = false
        }

        // Draw problems on map
        binding.mapView.getMapboxMap().loadStyle(layerFactory.buildStyle())

        addClickEvent()
    }

    @SuppressLint("ClickableViewAccessibility")
    // 1. Catch a tap on screen
    private fun addClickEvent() {
        binding.mapView.setOnTouchListener { _, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    shouldClick = true
                }
                MotionEvent.ACTION_UP -> {
                    if (shouldClick) {
                        installRenderedFeatures(event.x.toDouble(), event.y.toDouble())
                    }
                }
            }
            false
        }
    }

    // 2. Use x and y to determine whether or not it is relevant
    private fun installRenderedFeatures(x: Double, y: Double) {
        val geometry = RenderedQueryGeometry(
            ScreenCoordinate(x, y)
        )

        binding.mapView.getMapboxMap().apply {
            queryAreaRenderedFeatures(geometry) { moveCamera(it) }
            queryClusterRenderedFeatures(geometry) { moveCamera(it) }
            queryPoisRenderedFeatures(geometry) { name, url, geometry -> selectPois(name, url, geometry) }
            queryProblemRenderedFeatures(geometry, binding.mapView.height / 2) { selectProblem(it) }
        }
    }

    // Triggered when user click on a Area or Cluster on Map
    private fun moveCamera(coordinates: CoordinateBounds) {
        val cameraOption = CameraOptions.Builder()
            .center(coordinates.center())
            .bearing(0.0)
            .padding(EdgeInsets(60.0, 8.0, 8.0, 8.0))
            .pitch(0.0)
            .build()
        val mapAnimationOption = MapAnimationOptions.Builder()
            .duration(500L)
            .build()

        binding.mapView.getMapboxMap().flyTo(cameraOption, mapAnimationOption)
    }

    // Triggered when user click on a Parking on Map
    private fun selectPois(name: String, googleUrl: String, geometry: Geometry?) {
        //TODO
    }

    // Triggered when user click on a Problem on Map
    private fun selectProblem(problemId: Int) {
        lifecycleScope.launch {
            mapViewModel.fetchProblemAndTopo(problemId).collect { (problem, topo) ->
                val bottomSheetFragment = ProblemBSFragment.newInstance(problem, topo)
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            }
        }
    }
}