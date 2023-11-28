package com.boolder.boolder.view.map

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateDecelerateInterpolator
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
import com.boolder.boolder.R
import com.boolder.boolder.databinding.ActivityMainBinding
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.GradeRange
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.TopoOrigin
import com.boolder.boolder.utils.LocationProvider
import com.boolder.boolder.utils.MapboxStyleFactory
import com.boolder.boolder.utils.extension.launchAndCollectIn
import com.boolder.boolder.utils.viewBinding
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.BoolderMap.BoolderMapListener
import com.boolder.boolder.view.map.animator.animationEndListener
import com.boolder.boolder.view.map.composable.MapControlsOverlay
import com.boolder.boolder.view.map.filter.circuit.CircuitFilterBottomSheetDialogFragment
import com.boolder.boolder.view.map.filter.circuit.CircuitFilterBottomSheetDialogFragment.Companion.RESULT_CIRCUIT
import com.boolder.boolder.view.map.filter.grade.GradesFilterBottomSheetDialogFragment
import com.boolder.boolder.view.map.filter.grade.GradesFilterBottomSheetDialogFragment.Companion.RESULT_GRADE_RANGE
import com.boolder.boolder.view.offlinephotos.OfflinePhotosActivity
import com.boolder.boolder.view.search.SearchActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment.STYLE_NORMAL
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Double.max


class MapActivity : AppCompatActivity(), BoolderMapListener {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val mapViewModel by viewModel<MapViewModel>()
    private val layerFactory by inject<MapboxStyleFactory>()

    private val searchScreenLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult

        val resultData = result.data ?: return@registerForActivityResult

        when {
            resultData.hasExtra("AREA") -> flyToArea(
                requireNotNull(resultData.getParcelableExtra("AREA"))
            )

            resultData.hasExtra("PROBLEM") -> onProblemSelected(
                problemId = requireNotNull(resultData.getParcelableExtra<Problem>("PROBLEM")).id,
                origin = TopoOrigin.SEARCH
            )
        }
    }

    private lateinit var locationProvider: LocationProvider

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottomMargin = systemInsets.bottom + resources.getDimensionPixelSize(R.dimen.margin_map_controls)

            binding.mapView.applyInsets(systemInsets)

            binding.fabLocation
                .updateLayoutParams<MarginLayoutParams> { updateMargins(bottom = bottomMargin) }

            insets
        }

        locationProvider = LocationProvider(this)
        locationProvider.locationFlow.launchAndCollectIn(owner = this, collector = ::onGPSLocation)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.detailBottomSheet).also {
            it.skipCollapsed = true
            it.state = STATE_HIDDEN
            it.addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        STATE_EXPANDED -> mapViewModel.onProblemTopoVisibilityChanged(isVisible = true)
                        STATE_HIDDEN -> mapViewModel.onProblemTopoVisibilityChanged(isVisible = false)
                        else -> Unit
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        setupMap()

        binding.fabLocation.setOnClickListener {
            locationProvider.askForPosition()
        }

//        binding.offlinePhotosButton.setOnClickListener {
//            startActivity(Intent(this, OfflinePhotosActivity::class.java))
//        }

        binding.topoView.apply {
            onSelectProblemOnMap = { problemId ->
                binding.mapView.selectProblem(problemId)
                mapViewModel.updateCircuitControlsForProblem(problemId)
            }
            onCircuitProblemSelected = {
                mapViewModel.fetchTopo(problemId = it, origin = TopoOrigin.CIRCUIT)
            }
        }

        mapViewModel.topoStateFlow.launchAndCollectIn(owner = this, collector = ::onNewTopo)

        mapViewModel.screenStateFlow.launchAndCollectIn(owner = this) { screenState ->
            binding.controlsOverlayComposeView.setContent {
                BoolderTheme {
                    MapControlsOverlay(
                        offlineAreaItem = screenState.areaState,
                        circuitState = screenState.circuitState,
                        gradeState = screenState.gradeState,
                        popularState = screenState.popularFilterState,
                        shouldShowFiltersBar = screenState.shouldShowFiltersBar,
                        offlineAreaDownloader = mapViewModel,
                        onHideAreaName = ::onAreaLeft,
                        onSearchBarClicked = ::navigateToSearchScreen,
                        onCircuitFilterChipClicked = mapViewModel::onCircuitFilterChipClicked,
                        onGradeFilterChipClicked = mapViewModel::onGradeFilterChipClicked,
                        onPopularFilterChipClicked = mapViewModel::onPopularFilterChipClicked,
                        onResetFiltersClicked = mapViewModel::onResetFiltersButtonClicked,
                        onCircuitStartClicked = mapViewModel::onCircuitDepartureButtonClicked
                    )
                }
            }

            binding.mapView.apply {
                updateCircuit(screenState.circuitState?.circuitId?.toLong())
                applyFilters(
                    grades = screenState.gradeState.grades,
                    showPopular = screenState.popularFilterState.isEnabled
                )
            }
        }

        mapViewModel.eventFlow.launchAndCollectIn(owner = this) { event ->
            when (event) {
                is MapViewModel.Event.ShowAvailableCircuits -> showCircuitFilterBottomSheet(event)
                is MapViewModel.Event.ShowGradeRanges -> showGradesFilterBottomSheet(event)
                is MapViewModel.Event.ZoomOnCircuit -> zoomOnCircuit(event)
                is MapViewModel.Event.ZoomOnCircuitStartProblem -> onProblemSelected(event.problemId, TopoOrigin.CIRCUIT)
            }
        }

        supportFragmentManager.setFragmentResultListener(
            /* requestKey = */ CircuitFilterBottomSheetDialogFragment.REQUEST_KEY,
            /* lifecycleOwner = */ this
        ) { _, bundle ->
            val circuit = bundle.getParcelable<Circuit?>(RESULT_CIRCUIT)

            mapViewModel.onCircuitSelected(circuit)
        }

        supportFragmentManager.setFragmentResultListener(
            /* requestKey = */ GradesFilterBottomSheetDialogFragment.REQUEST_KEY,
            /* lifecycleOwner = */ this
        ) { _, bundle ->
            val gradeRange = requireNotNull(bundle.getParcelable<GradeRange>(RESULT_GRADE_RANGE))

            mapViewModel.onGradeRangeSelected(gradeRange)
        }
    }

    override fun onDestroy() {
        binding.topoView.apply {
            onSelectProblemOnMap = null
            onCircuitProblemSelected = null
        }
        super.onDestroy()
    }

    private fun onGPSLocation(location: Location) {
        val point = Point.fromLngLat(location.longitude, location.latitude)
        val zoomLevel = max(binding.mapView.getMapboxMap().cameraState.zoom, 17.0)

        binding.mapView.getMapboxMap()
            .setCamera(CameraOptions.Builder().center(point).zoom(zoomLevel).bearing(location.bearing.toDouble()).build())
        binding.mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

    private fun setupMap() {
        binding.mapView.setup(this, layerFactory.buildStyle())
    }

    // Triggered when user click on a Problem on Map
    override fun onProblemSelected(problemId: Int, origin: TopoOrigin) {
        mapViewModel.fetchTopo(problemId = problemId, origin = origin)
    }

    override fun onProblemUnselected() {
        bottomSheetBehavior.state = STATE_HIDDEN
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

    override fun onAreaVisited(areaId: Int) {
        mapViewModel.onAreaVisited(areaId)
    }

    override fun onAreaLeft() {
        mapViewModel.onAreaLeft()
    }

    override fun onZoomLevelChanged(zoomLevel: Double) {
        mapViewModel.onZoomLevelChanged(zoomLevel)
    }

    private fun onNewTopo(nullableTopo: Topo?) {
        nullableTopo?.let { topo ->
            binding.topoView.setTopo(topo)

            val selectedProblem = topo.selectedCompleteProblem
                ?.problemWithLine
                ?.problem
                ?: return@let

            flyToProblem(problem = selectedProblem, origin = topo.origin)
        }
        bottomSheetBehavior.state = if (nullableTopo == null) STATE_HIDDEN else STATE_EXPANDED
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

    private fun flyToArea(area: Area) {
        val southWest = Point.fromLngLat(
            area.southWestLon.toDouble(),
            area.southWestLat.toDouble()
        )
        val northEst = Point.fromLngLat(
            area.northEastLon.toDouble(),
            area.northEastLat.toDouble()
        )
        val coordinates = CoordinateBounds(southWest, northEst)

        val cameraOptions = binding.mapView.getMapboxMap().cameraForCoordinateBounds(
            coordinates,
            EdgeInsets(60.0, 8.0, 8.0, 8.0),
            0.0,
            0.0
        )

        binding.mapView.camera.flyTo(
            cameraOptions = cameraOptions,
            animationOptions = defaultMapAnimationOptions {
                animatorListener(animationEndListener { onAreaVisited(area.id) })
            }
        )

        bottomSheetBehavior.state = STATE_HIDDEN
    }

    private fun flyToProblem(problem: Problem, origin: TopoOrigin) {
        binding.mapView.selectProblem(problem.id.toString())

        val point = Point.fromLngLat(
            problem.longitude.toDouble(),
            problem.latitude.toDouble()
        )

        val zoomLevel = binding.mapView.getMapboxMap().cameraState.zoom

        val cameraOptions = CameraOptions.Builder().run {
            if (origin in arrayOf(TopoOrigin.SEARCH, TopoOrigin.CIRCUIT)) center(point)

            padding(EdgeInsets(40.0, 0.0, binding.mapView.height / 2.0, 0.0))
            zoom(if (zoomLevel <= 19.0) 20.0 else zoomLevel)
            build()
        }

        binding.mapView.camera.easeTo(
            cameraOptions = cameraOptions,
            animationOptions = defaultMapAnimationOptions {
                animatorListener(animationEndListener { onAreaVisited(problem.areaId) })
            }
        )
    }

    private fun defaultMapAnimationOptions(block: MapAnimationOptions.Builder.() -> Unit) =
        MapAnimationOptions.mapAnimationOptions {
            duration(300L)
            interpolator(AccelerateDecelerateInterpolator())
            block()
        }

    private fun navigateToSearchScreen() {
        val intent = Intent(this, SearchActivity::class.java)
        val option = ActivityOptionsCompat.makeSceneTransitionAnimation(this)

        searchScreenLauncher.launch(intent, option)
    }

    private fun showCircuitFilterBottomSheet(event: MapViewModel.Event.ShowAvailableCircuits) {
        CircuitFilterBottomSheetDialogFragment.newInstance(event.availableCircuits)
            .apply { setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme) }
            .show(supportFragmentManager, CircuitFilterBottomSheetDialogFragment.TAG)
    }

    private fun showGradesFilterBottomSheet(event: MapViewModel.Event.ShowGradeRanges) {
        GradesFilterBottomSheetDialogFragment.newInstance(event.currentGradeRange)
            .apply { setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme) }
            .show(supportFragmentManager, GradesFilterBottomSheetDialogFragment.TAG)
    }

    private fun zoomOnCircuit(event: MapViewModel.Event.ZoomOnCircuit) {
        bottomSheetBehavior.state = STATE_HIDDEN
        binding.mapView.onCircuitSelected(event.circuit)
    }
}
