package com.boolder.boolder.view.map

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.boolder.boolder.R
import com.boolder.boolder.databinding.FragmentMapBinding
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.GradeRange
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.domain.model.TopoOrigin
import com.boolder.boolder.utils.LocationProvider
import com.boolder.boolder.utils.MapboxStyleFactory
import com.boolder.boolder.utils.extension.containsCameraState
import com.boolder.boolder.utils.extension.getCameraOptions
import com.boolder.boolder.utils.extension.launchAndCollectIn
import com.boolder.boolder.utils.extension.putCameraState
import com.boolder.boolder.view.areadetails.KEY_AREA_ID
import com.boolder.boolder.view.areadetails.KEY_CIRCUIT_ID
import com.boolder.boolder.view.areadetails.KEY_PROBLEM
import com.boolder.boolder.view.areadetails.REQUEST_KEY_AREA_DETAILS
import com.boolder.boolder.view.compose.BoolderTheme
import com.boolder.boolder.view.map.BoolderMap.BoolderMapListener
import com.boolder.boolder.view.map.animator.animationEndListener
import com.boolder.boolder.view.map.composable.MapControlsOverlay
import com.boolder.boolder.view.map.filter.circuit.CircuitFilterBottomSheetDialogFragment
import com.boolder.boolder.view.map.filter.circuit.CircuitFilterBottomSheetDialogFragment.Companion.RESULT_CIRCUIT_ID
import com.boolder.boolder.view.map.filter.grade.GradesFilterBottomSheetDialogFragment
import com.boolder.boolder.view.map.filter.grade.GradesFilterBottomSheetDialogFragment.Companion.RESULT_GRADE_RANGE
import com.boolder.boolder.view.search.SearchFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.coroutine.mapLoadedEvents
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Double.max

class MapFragment : Fragment(), BoolderMapListener {

    private var binding: FragmentMapBinding? = null

    private val mapViewModel by viewModel<MapViewModel>()
    private val layerFactory by inject<MapboxStyleFactory>()

    private lateinit var locationProvider: LocationProvider

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    private val onBackPressedCallback = object : OnBackPressedCallback(enabled = false) {
        override fun handleOnBackPressed() {
            if (bottomSheetBehavior.state != STATE_HIDDEN) onTopoUnselected()
        }
    }

    private lateinit var mapView: BoolderMap
    private var pendingMapAction: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationProvider = LocationProvider(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentMapBinding = FragmentMapBinding.inflate(inflater, container, false)

        val cameraOptions = mapViewModel.cameraState?.let { cameraState ->
            CameraOptions.Builder()
                .center(cameraState.center)
                .padding(cameraState.padding)
                .zoom(cameraState.zoom)
                .build()
        } ?: CameraOptions.Builder()
            .center(Point.fromLngLat(2.5968216, 48.3925623))
            .zoom(10.2)
            .build()

        mapViewModel.cameraState = null

        mapView = BoolderMap(
            context = inflater.context,
            mapInitOptions = MapInitOptions(
                context = inflater.context,
                cameraOptions = cameraOptions
            )
        )

        fragmentMapBinding.mapContainer.addView(mapView)

        binding = fragmentMapBinding

        return fragmentMapBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = binding ?: return

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            mapView.applyInsets(systemInsets)
            binding.fabLocation.updateLayoutParams<MarginLayoutParams> {
                val bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_map_controls)
                val bottomNavHeight = resources.getDimensionPixelSize(R.dimen.height_bottom_nav_bar)

                updateMargins(bottom = bottomMargin + systemInsets.bottom + bottomNavHeight)
            }
            binding.topoView.applyInsets(systemInsets)

            insets
        }

        locationProvider.locationFlow.launchAndCollectIn(owner = this, collector = ::onGPSLocation)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.detailBottomSheet).also {
            it.skipCollapsed = true
            it.state = STATE_HIDDEN
            it.addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        STATE_EXPANDED -> mapViewModel.onProblemTopoVisibilityChanged(isVisible = true)
                        STATE_HIDDEN -> {
                            mapViewModel.onProblemTopoVisibilityChanged(isVisible = false)
                            onBackPressedCallback.isEnabled = false
                        }
                        else -> Unit
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        mapView.apply {
            setup(this@MapFragment, layerFactory.buildStyle())

            mapboxMap.mapLoadedEvents.launchAndCollectIn(viewLifecycleOwner) {
                pendingMapAction?.invoke()
                    ?: postDelayed(1_000L) { detectArea() }
                pendingMapAction = null
            }
        }

        binding.fabLocation.setOnClickListener {
            locationProvider.askForPosition()
        }

        binding.topoView.apply {
            onSelectProblemOnMap = { problemId ->
                mapView.selectProblem(problemId)
                mapViewModel.updateCircuitControlsForProblem(problemId)
            }
            onCircuitProblemSelected = {
                mapViewModel.fetchTopo(problemId = it, origin = TopoOrigin.CIRCUIT)
            }
            onShowProblemPhotoFullScreen = ::navigateToFullScreenProblemPhoto
            tickedProblemSaver = mapViewModel
        }

        mapViewModel.topoStateFlow.launchAndCollectIn(owner = this, collector = ::onNewTopo)

        mapViewModel.screenStateFlow.launchAndCollectIn(owner = this) { screenState ->
            binding.controlsOverlayComposeView.setContent {
                BoolderTheme {
                    MapControlsOverlay(
                        modifier = Modifier.padding(
                            bottom = dimensionResource(id = R.dimen.height_bottom_nav_bar)
                        ),
                        offlineAreaItem = screenState.areaState,
                        circuitState = screenState.circuitState,
                        gradeState = screenState.gradeState,
                        popularState = screenState.popularFilterState,
                        projectsState = screenState.projectsFilterState,
                        tickedState = screenState.tickedFilterState,
                        shouldShowFiltersBar = screenState.shouldShowFiltersBar,
                        filtersEventHandler = mapViewModel,
                        onHideAreaName = ::onAreaLeft,
                        onAreaInfoClicked = { navigateToAreaOverviewScreen(screenState.areaState?.area?.id) },
                        onSearchBarClicked = ::navigateToSearchScreen,
                        onCircuitStartClicked = mapViewModel::onCircuitDepartureButtonClicked
                    )
                }
            }

            mapView.apply {
                updateCircuit(screenState.circuitState?.circuitId?.toLong())
                applyFilters(
                    grades = screenState.gradeState.grades,
                    showPopular = screenState.popularFilterState.isEnabled,
                    projectIds = screenState.projectsFilterState.projectIds,
                    tickedIds = screenState.tickedFilterState.tickedProblemIds
                )
            }
        }

        mapViewModel.eventFlow.launchAndCollectIn(owner = viewLifecycleOwner) { event ->
            when (event) {
                is MapViewModel.Event.ShowAvailableCircuits -> showCircuitFilterBottomSheet(event)
                is MapViewModel.Event.ShowGradeRanges -> showGradesFilterBottomSheet(event)
                is MapViewModel.Event.ZoomOnCircuit -> zoomOnCircuit(event)
                is MapViewModel.Event.ZoomOnCircuitStartProblem -> onProblemSelected(event.problemId, TopoOrigin.CIRCUIT)
                is MapViewModel.Event.ZoomOnArea -> flyToArea(event.area)
                is MapViewModel.Event.WarnNoSavedProjects -> showNoSavedProjectsDialog()
                is MapViewModel.Event.WarnNoTickedProblems -> showNoTickedProblemsDialog()
            }
        }

        parentFragmentManager.setFragmentResultListener(
            /* requestKey = */ SearchFragment.REQUEST_KEY,
            /* lifecycleOwner = */ this
        ) { _, bundle ->
            pendingMapAction = {
                when {
                    bundle.containsKey("AREA") -> flyToArea(requireNotNull(bundle.getParcelable("AREA")))

                    bundle.containsKey("PROBLEM") -> onProblemSelected(
                        problemId = requireNotNull(bundle.getParcelable<Problem>("PROBLEM")).id,
                        origin = TopoOrigin.SEARCH
                    )
                }
            }
        }

        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY_AREA_DETAILS,
            this
        ) { _, bundle ->
            pendingMapAction = {
                when {
                    bundle.containsKey(KEY_PROBLEM) -> onProblemSelected(
                        problemId = requireNotNull(bundle.getParcelable<Problem>(KEY_PROBLEM)).id,
                        origin = TopoOrigin.SEARCH
                    )

                    bundle.containsKey(KEY_CIRCUIT_ID) -> mapViewModel.onCircuitSelected(
                        circuitId = requireNotNull(bundle.getInt(KEY_CIRCUIT_ID))
                    )

                    bundle.containsKey(KEY_AREA_ID) -> mapViewModel.onAreaSelected(
                        areaId = requireNotNull(bundle.getInt(KEY_AREA_ID))
                    )
                }
            }
        }

        parentFragmentManager.setFragmentResultListener(
            /* requestKey = */ CircuitFilterBottomSheetDialogFragment.REQUEST_KEY,
            /* lifecycleOwner = */ this
        ) { _, bundle ->
            val circuitId = bundle.getInt(RESULT_CIRCUIT_ID)

            mapViewModel.onCircuitSelected(circuitId)
        }

        parentFragmentManager.setFragmentResultListener(
            /* requestKey = */ GradesFilterBottomSheetDialogFragment.REQUEST_KEY,
            /* lifecycleOwner = */ this
        ) { _, bundle ->
            val gradeRange = requireNotNull(bundle.getParcelable<GradeRange>(RESULT_GRADE_RANGE))

            mapViewModel.onGradeRangeSelected(gradeRange)
        }

        arguments?.getString("problem_id")?.toIntOrNull()?.let { problemId ->
            mapViewModel.fetchTopo(problemId = problemId, origin = TopoOrigin.DEEP_LINK)
        }
    }

    override fun onPause() {
        mapViewModel.cameraState = mapView.mapboxMap.cameraState

        super.onPause()
    }

    override fun onDestroyView() {
        binding?.let {
            it.topoView.apply {
                onSelectProblemOnMap = null
                onCircuitProblemSelected = null
                onShowProblemPhotoFullScreen = null
                tickedProblemSaver = null
            }

            it.mapContainer.removeView(mapView)
        }

        binding = null

        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putCameraState(mapView.mapboxMap.cameraState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState?.containsCameraState() == true) {
            mapView.mapboxMap.setCamera(savedInstanceState.getCameraOptions())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun onGPSLocation(location: Location) {
        val point = Point.fromLngLat(location.longitude, location.latitude)
        val zoomLevel = max(mapView.mapboxMap.cameraState.zoom, 17.0)

        mapView.mapboxMap
            .setCamera(CameraOptions.Builder().center(point).zoom(zoomLevel).bearing(location.bearing.toDouble()).build())
        mapView.location.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
    }

    // Triggered when user click on a Problem on Map
    override fun onProblemSelected(problemId: Int, origin: TopoOrigin) {
        mapViewModel.fetchTopo(problemId = problemId, origin = origin)
    }

    override fun onTopoUnselected() {
        bottomSheetBehavior.state = STATE_HIDDEN
        mapViewModel.onTopoUnselected()
    }

    override fun onPoiSelected(poiName: String, googleMapsUrl: String) {
        val direction = MapFragmentDirections.showPoi(
            poiName = poiName,
            googleMapsUrl = googleMapsUrl
        )

        findNavController().navigate(direction)
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
            val binding = binding ?: return

            binding.topoView.setTopo(topo)

            val selectedProblem = topo.selectedCompleteProblem
                ?.problemWithLine
                ?.problem
                ?: return@let

            flyToProblem(problem = selectedProblem, origin = topo.origin)
        }

        if (nullableTopo == null) {
            bottomSheetBehavior.state = STATE_HIDDEN
        } else {
            onBackPressedCallback.isEnabled = true
            mapView.post { bottomSheetBehavior.state = STATE_EXPANDED }
        }
    }

    private fun flyToArea(area: Area) {
        val southWest = Point.fromLngLat(
            area.southWestLon.toDouble(),
            area.southWestLat.toDouble()
        )
        val northEast = Point.fromLngLat(
            area.northEastLon.toDouble(),
            area.northEastLat.toDouble()
        )

        val cameraOptions = mapView.mapboxMap.cameraForCoordinates(
            coordinates = listOf(southWest, northEast),
            camera = CameraOptions.Builder().build(),
            coordinatesPadding = EdgeInsets(60.0, 8.0, 8.0, 8.0),
            maxZoom = null,
            offset = null
        )

        mapView.camera.flyTo(
            cameraOptions = cameraOptions,
            animationOptions = defaultMapAnimationOptions {},
            animatorListener = animationEndListener { delayedVisitToArea(area.id) }
        )

        bottomSheetBehavior.state = STATE_HIDDEN
    }

    private fun flyToProblem(problem: Problem, origin: TopoOrigin) {
        mapView.selectProblem(problem.id.toString())

        val point = Point.fromLngLat(
            problem.longitude.toDouble(),
            problem.latitude.toDouble()
        )

        val zoomLevel = mapView.mapboxMap.cameraState.zoom

        val cameraOptions = CameraOptions.Builder().run {
            if (origin in arrayOf(TopoOrigin.SEARCH, TopoOrigin.CIRCUIT, TopoOrigin.DEEP_LINK)) {
                center(point)
            }

            padding(EdgeInsets(40.0, 0.0, mapView.height / 2.0, 0.0))
            zoom(if (zoomLevel <= 19.0) 20.0 else zoomLevel)
            build()
        }

        mapView.camera.flyTo(
            cameraOptions = cameraOptions,
            animationOptions = defaultMapAnimationOptions {},
            animatorListener = animationEndListener { delayedVisitToArea(problem.areaId) }
        )
    }

    private fun delayedVisitToArea(areaId: Int) {
        mapView.postDelayed(500L) { onAreaVisited(areaId) }
    }

    private fun defaultMapAnimationOptions(block: MapAnimationOptions.Builder.() -> Unit) =
        MapAnimationOptions.mapAnimationOptions {
            duration(300L)
            interpolator(AccelerateDecelerateInterpolator())
            block()
        }

    private fun navigateToAreaOverviewScreen(areaId: Int?) {
        areaId ?: return

        val direction = MapFragmentDirections.navigateToAreaOverviewScreen(areaId = areaId)

        onTopoUnselected()
        findNavController().navigate(direction)
    }

    private fun navigateToSearchScreen() {
        onTopoUnselected()
        findNavController().navigate(MapFragmentDirections.navigateToSearch())
    }

    private fun navigateToFullScreenProblemPhoto(problemId: Int, photoUri: String) {
        val direction = MapFragmentDirections.showProblemPhotoFullScreen(
            problemId = problemId,
            photoUri = photoUri
        )

        findNavController().navigate(direction)
    }

    private fun showCircuitFilterBottomSheet(event: MapViewModel.Event.ShowAvailableCircuits) {
        val navController = findNavController()

        if (navController.currentDestination?.id == R.id.dialog_circuit_filter) return

        val direction = MapFragmentDirections.showCircuitsFilter(
            availableCircuits = event.availableCircuits.toTypedArray()
        )

        navController.navigate(direction)
    }

    private fun showGradesFilterBottomSheet(event: MapViewModel.Event.ShowGradeRanges) {
        val navController = findNavController()

        if (navController.currentDestination?.id == R.id.dialog_grades_filter) return

        val direction = MapFragmentDirections.showGradesFilter(gradeRange = event.currentGradeRange)

        navController.navigate(direction)
    }

    private fun showNoSavedProjectsDialog() {
        val context = context ?: return

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.filter_warning_no_projects_title)
            .setMessage(R.string.filter_warning_no_projects_message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showNoTickedProblemsDialog() {
        val context = context ?: return

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.filter_warning_no_ticks_title)
            .setMessage(R.string.filter_warning_no_ticks_message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun zoomOnCircuit(event: MapViewModel.Event.ZoomOnCircuit) {
        onTopoUnselected()
        mapView.onCircuitSelected(event.circuit)
    }
}
