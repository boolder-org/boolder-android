package com.boolder.boolder.view.map

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.lifecycle.lifecycleScope
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ActivityMainBinding
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.LocationCallback
import com.boolder.boolder.utils.LocationProvider
import com.boolder.boolder.utils.MapboxStyleFactory
import com.boolder.boolder.utils.extension.setOnApplyWindowTopInsetListener
import com.boolder.boolder.utils.viewBinding
import com.boolder.boolder.view.detail.BottomSheetListener
import com.boolder.boolder.view.detail.ProblemBSFragment
import com.boolder.boolder.view.map.BoolderMap.BoolderClickListener
import com.boolder.boolder.view.search.SearchActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapActivity : AppCompatActivity(), LocationCallback, BoolderClickListener, BottomSheetListener {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val mapViewModel by viewModel<MapViewModel>()
    private val layerFactory: MapboxStyleFactory by inject()

    private lateinit var locationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.root.setOnApplyWindowTopInsetListener { topInset ->
            val topMargin = topInset + resources.getDimensionPixelSize(R.dimen.margin_search_component)

            binding.searchComponent
                .searchContainer
                .updateLayoutParams<ViewGroup.MarginLayoutParams> { updateMargins(top = topMargin) }
        }

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
                        binding.mapView.getMapboxMap().cameraForCoordinateBounds(
                            coordinates,
                            EdgeInsets(60.0, 8.0, 8.0, 8.0),
                            0.0,
                            0.0
                        )
                    } else if (it.hasExtra("PROBLEM")) {
                        val problem = it.getParcelableExtra<Problem>("PROBLEM")
                        onProblemSelected(problem!!.id)
                        binding.mapView.selectProblem(problem.id.toString())
                        val point = Point.fromLngLat(problem.longitude.toDouble(), problem.latitude.toDouble())
                        CameraOptions.Builder().center(point).zoom(20.0).build()
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
                    val bottomSheetFragment = ProblemBSFragment.newInstance(completeProblem, this@MapActivity)
                    bottomSheetFragment.setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }
            }
        }
    }

    override fun onPoisSelected(poisName: String, stringProperty: String, geometry: Geometry?) {

        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_pois, binding.root, false)
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)

        view.apply {
            findViewById<TextView>(R.id.pois_title).text = poisName
            findViewById<Button>(R.id.open).setOnClickListener {
                openGoogleMaps(stringProperty)
            }
            findViewById<Button>(R.id.close).setOnClickListener { bottomSheet.dismiss() }
        }
        bottomSheet.setContentView(view)
        bottomSheet.show()

    }

    private fun openGoogleMaps(url: String) {

        val sendIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val shareIntent = Intent.createChooser(sendIntent, null)
        try {
            startActivity(shareIntent)
        } catch (e: Exception) {
            Log.i("MAP", "No apps can handle this kind of intent")
        }
    }

    override fun onProblemSelected(problem: Problem) {
        binding.mapView.selectProblemAndCenter(problem)
    }
}