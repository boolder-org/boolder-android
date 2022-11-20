package com.boolder.boolder.view.map

import android.app.ActivityOptions
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ActivityMainBinding
import com.boolder.boolder.utils.LocationCallback
import com.boolder.boolder.utils.LocationProvider
import com.boolder.boolder.utils.MapboxStyleFactory
import com.boolder.boolder.utils.viewBinding
import com.boolder.boolder.view.detail.ProblemBSFragment
import com.boolder.boolder.view.map.BoolderMap.BoolderClickListener
import com.boolder.boolder.view.search.SearchActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapActivity : AppCompatActivity(), LocationCallback, BoolderClickListener {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val mapViewModel by viewModel<MapViewModel>()
    private val layerFactory: MapboxStyleFactory by inject()

    private lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        locationProvider = LocationProvider(this, this)

        setupMap()

        binding.fabLocation.setOnClickListener {
            locationProvider.askForPosition()
        }

        binding.searchComponent.searchBar.isFocusable = false
        binding.searchComponent.searchBar.isClickable = false
        binding.searchComponent.searchBar.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            val option: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this)
            startActivity(intent, option.toBundle())
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
        binding.mapView.getMapboxMap().loadStyle(layerFactory.buildStyle())
        binding.mapView.setOnBoolderClickListener(this)
    }

    // Triggered when user click on a Problem on Map
    override fun onProblemSelected(problemId: Int) {
        lifecycleScope.launch {
            mapViewModel.fetchProblemAndTopo(problemId).collect { (problem, topo) ->
                val bottomSheetFragment = ProblemBSFragment.newInstance(problem, topo)
                bottomSheetFragment.setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            }
        }
    }

    override fun onPoisSelected(poisId: String, stringProperty: String, geometry: Geometry?) {

    }
}