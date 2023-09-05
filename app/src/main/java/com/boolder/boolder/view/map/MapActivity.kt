package com.boolder.boolder.view.map

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.lifecycle.lifecycleScope
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ActivityMainBinding
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.GradeRange
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.LocationCallback
import com.boolder.boolder.utils.LocationProvider
import com.boolder.boolder.utils.MapboxStyleFactory
import com.boolder.boolder.utils.extension.launchAndCollectIn
import com.boolder.boolder.utils.viewBinding
import com.boolder.boolder.view.map.BoolderMap.BoolderClickListener
import com.boolder.boolder.view.map.filter.GradesFilterBottomSheetDialogFragment
import com.boolder.boolder.view.map.filter.GradesFilterBottomSheetDialogFragment.Companion.RESULT_GRADE_RANGE
import com.boolder.boolder.view.search.SearchActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment.STYLE_NORMAL
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapActivity : AppCompatActivity(), LocationCallback, BoolderClickListener {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val mapViewModel by viewModel<MapViewModel>()
    private val layerFactory by inject<MapboxStyleFactory>()

    private lateinit var locationProvider: LocationProvider

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val topMargin = systemInsets.top + resources.getDimensionPixelSize(R.dimen.margin_search_component)
            val bottomMargin = systemInsets.bottom + resources.getDimensionPixelSize(R.dimen.margin_map_controls)

            binding.searchComponent
                .searchContainer
                .updateLayoutParams<MarginLayoutParams> { updateMargins(top = topMargin) }

            binding.mapView.applyCompassTopInset(systemInsets.top.toFloat())

            binding.gradesFilterButton
                .updateLayoutParams<MarginLayoutParams> { updateMargins(bottom = bottomMargin) }

            binding.fabLocation
                .updateLayoutParams<MarginLayoutParams> { updateMargins(bottom = bottomMargin) }

            WindowInsetsCompat.CONSUMED
        }

        locationProvider = LocationProvider(this, this)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.detailBottomSheet)
            .also { it.state = BottomSheetBehavior.STATE_HIDDEN }

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

        binding.gradesFilterButton.setOnClickListener {
            mapViewModel.withCurrentGradeRange {
                GradesFilterBottomSheetDialogFragment.newInstance(it)
                    .apply { setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme) }
                    .show(supportFragmentManager, GradesFilterBottomSheetDialogFragment.TAG)
            }
        }

        mapViewModel.gradeStateFlow.launchAndCollectIn(owner = this) {
            binding.mapView.filterGrades(it.grades)
            binding.gradesFilterButton.text = it.gradeRangeButtonTitle
        }

        supportFragmentManager.setFragmentResultListener(
            /* requestKey = */ GradesFilterBottomSheetDialogFragment.REQUEST_KEY,
            /* lifecycleOwner = */ this
        ) { _, bundle ->
            val gradeRange = requireNotNull(bundle.getParcelable<GradeRange>(RESULT_GRADE_RANGE))

            mapViewModel.onGradeRangeSelected(gradeRange)
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
                binding.problemView.setProblem(completeProblem)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onProblemUnselected() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
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
}
