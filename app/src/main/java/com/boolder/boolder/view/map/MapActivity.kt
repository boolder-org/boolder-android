package com.boolder.boolder.view.map

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ActivityMainBinding
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Problem
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
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.Dispatchers
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

        val searchRegister = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let {
                    val cameraOptions = if (it.hasExtra("AREA")) {
                        val area = it.getParcelableExtra<Area>("AREA")
                        val southWest = Point.fromLngLat(area!!.southWestLon.toDouble(), area.southWestLat.toDouble())
                        val northEst = Point.fromLngLat(area.northEastLon.toDouble(), area.northEastLat.toDouble())
                        val coordinates = CoordinateBounds(southWest, northEst)
                        CameraOptions.Builder().center(coordinates.center()).build()
                    } else if (it.hasExtra("PROBLEM")) {
                        val problem = it.getParcelableExtra<Problem>("PROBLEM")
                        onProblemSelected(problem!!.id)
                        binding.mapView.selectProblem(problem.id.toString())
                        val point = Point.fromLngLat(problem.longitude.toDouble(), problem.latitude.toDouble())
                        CameraOptions.Builder().center(point).zoom(22.0).build()
                    } else null

                    cameraOptions?.let { option ->
                        binding.mapView.camera.flyTo(option)
                    }
                }
            }
        }

        binding.searchComponent.searchBar.isFocusable = false
        binding.searchComponent.searchBar.isClickable = false
        binding.searchComponent.searchBar.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
            searchRegister.launch(intent, option)
        }
    }

    override fun onGPSLocation(location: Location) {
        val point = Point.fromLngLat(location.longitude, location.latitude)

        binding.mapView.getMapboxMap()
            .setCamera(CameraOptions.Builder().center(point).zoom(16.0).bearing(location.bearing.toDouble()).build())
        binding.mapView.gestures.focalPoint = binding.mapView.getMapboxMap().pixelForCoordinate(point)
        binding.mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

    private fun setupMap() {
        binding.mapView.setup(this, layerFactory.buildStyle())
    }

    // Triggered when user click on a Problem on Map
    override fun onProblemSelected(problemId: Int) {
        lifecycleScope.launch {
            mapViewModel.fetchProblemAndTopo(problemId).collect { completeProblem ->
                with(Dispatchers.Main) {
                    val bottomSheetFragment = ProblemBSFragment.newInstance(completeProblem)
                    bottomSheetFragment.setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }
            }
        }
    }

    override fun onPoisSelected(poisId: String, stringProperty: String, geometry: Geometry?) {
        val sendIntent = Intent(Intent.ACTION_VIEW, Uri.parse(stringProperty))
        val shareIntent = Intent.createChooser(sendIntent, null)
        try {
            startActivity(shareIntent)
        } catch (e: Exception) {
            Log.i("MAP", "No apps can handle this kind of intent")
        }
    }
}