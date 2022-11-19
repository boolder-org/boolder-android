package com.boolder.boolder

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.boolder.boolder.databinding.ActivityMainBinding
import com.boolder.boolder.view.map.LocationCallback
import com.boolder.boolder.view.map.LocationProvider
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.generated.vectorSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity(), LocationCallback {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val mainViewModel by viewModel<MainViewModel>()

    private lateinit var locationProvider: LocationProvider

    private val styleUri = "mapbox://styles/nmondollot/cl95n147u003k15qry7pvfmq2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        locationProvider = LocationProvider(this, this)

        setupMap()
        addLayers()
        addClickEvent()

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
    }

    // Draw problems on map
    private fun addLayers() {
        val problemsSourceLayerId = "problems-ayes3a" // name of the layer in the mapbox tileset

        binding.mapView.getMapboxMap().loadStyle(
            style(styleUri) {
                +vectorSource("problems") {
                    url("mapbox://nmondollot.4xsv235p")
                }
                +circleLayer("problems", "problems") {
                    sourceLayer(problemsSourceLayerId)
                    minZoom(15.0)
                    filter(Expression.match {
                        geometryType()
                        get("Point")
                        literal(true)
                        literal(false)
                    })

                    circleRadius(
                        interpolate {
                            linear()
                            zoom()
                            literal(15.0)
                            literal(2.0)
                            literal(18.0)
                            literal(4.0)
                            literal(22.0)
                            switchCase {
                                boolean {
                                    has("circuitColor")
                                    literal(false)
                                }
                                literal(16.0)
                                literal(10.0)
                            }
                        }
                    )

                    circleColor(
                        match {
                            get { literal("circuitColor") }
                            literal("yellow")
                            rgb(255.0, 204.0, 2.0)
                            literal("purple")
                            rgb(215.0, 131.0, 255.0)
                            literal("orange")
                            rgb(255.0, 149.0, 0.0)
                            literal("green")
                            rgb(255.0, 149.0, 0.0)
                            literal("blue")
                            rgb(1.0, 122.0, 255.0)
                            literal("skyblue")
                            rgb(90.0, 199.0, 250.0)
                            literal("salmon")
                            rgb(253.0, 175.0, 138.0)
                            literal("red")
                            rgb(255.0, 59.0, 47.0)
                            literal("black")
                            rgb(0.0, 0.0, 0.0)
                            literal("white")
                            rgb(255.0, 255.0, 255.0)
                            rgb(135.0, 138.0, 141.0)
                        }
                    )

                    circleStrokeWidth(
                        Expression.switchCase {
                            boolean {
                                featureState { literal("selected") }
                                literal(false)
                            }
                            literal(3.0)
                            literal(0.0)
                        }
                    )

                    circleStrokeColor(ContextCompat.getColor(this@MainActivity, R.color.primary))
                    circleSortKey(
                        switchCase {
                            boolean {
                                has { literal("circuitId") }
                                literal(false)
                            }
                            literal(2)
                            literal(1)
                        }

                    )
                }
                +symbolLayer("problems-text", "problems") {
                    sourceLayer(problemsSourceLayerId)
                    minZoom(19.0)
                    filter(Companion.match {
                        geometryType()
                        get("Point")
                        literal(true)
                        literal(false)
                    })
                    textAllowOverlap(true)
                    textField(
                        get("circuitNumber")
                    )
                    textSize(interpolate {
                        linear()
                        zoom()
                        literal(19)
                        literal(10)
                        literal(22)
                        literal(20)
                    })
                    textColor(switchCase {
                        match {
                            get("circuitColor")
                            literal("white")
                            literal(true)
                            literal(false)
                        }
                        rgb(0.0, 0.0, 0.0)
                        rgb(255.0, 255.0, 255.0)
                    })
                }
            }
        )
    }

    private var shouldClick = false

    @SuppressLint("ClickableViewAccessibility")
    // 1. Catch a tap on screen
    private fun addClickEvent() {
        binding.mapView.setOnTouchListener { v, event: MotionEvent ->
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
        val problemGeometry = RenderedQueryGeometry(
            ScreenCoordinate(x - 12.0, y - 12.0)
        )

        val areaOption = RenderedQueryOptions(
            listOf("areas", "areas-hulls"),
            Expression.lt(
                Companion.zoom(),
                Companion.literal(15.0)
            )
        )

        val clusterOption = RenderedQueryOptions(listOf("cluster"), null)
        val poisOption = RenderedQueryOptions(listOf("pois"), null)
        val problemsOption = RenderedQueryOptions(listOf("problems"), null)

        binding.mapView.getMapboxMap()
            .queryRenderedFeatures(geometry, areaOption) { features: Expected<String, MutableList<QueriedFeature>> ->
                buildCoordinateBounds(features)
            }

        binding.mapView.getMapboxMap()
            .queryRenderedFeatures(geometry, clusterOption) { features: Expected<String, MutableList<QueriedFeature>> ->
                buildCoordinateBounds(features)
            }

        binding.mapView.getMapboxMap()
            .queryRenderedFeatures(geometry, poisOption) { features: Expected<String, MutableList<QueriedFeature>> ->
                buildParkingSelection(features)
            }

        binding.mapView.getMapboxMap()
            .queryRenderedFeatures(
                problemGeometry,
                problemsOption
            ) { features: Expected<String, MutableList<QueriedFeature>> ->
                buildProblemsSelection(features, y)
            }
    }

    // 3A. Build bounds around coordinate
    private fun buildCoordinateBounds(features: Expected<String, MutableList<QueriedFeature>>) {
        if (features.isValue) {
            features.value?.firstOrNull()?.feature?.let {
                if (it.hasProperty("southWestLon") &&
                    it.hasProperty("southWestLat") &&
                    it.hasProperty("northEastLon") &&
                    it.hasProperty("northEastLat")
                ) {
                    val southWest = Point.fromLngLat(
                        it.getStringProperty("southWestLon").toDouble(),
                        it.getStringProperty("southWestLat").toDouble()
                    )
                    val northEst = Point.fromLngLat(
                        it.getStringProperty("northEastLon").toDouble(),
                        it.getStringProperty("northEastLat").toDouble()
                    )
                    val coordinateBound = CoordinateBounds(southWest, northEst)
                    moveCamera(coordinateBound)
                }
            }
        } else {
            Log.w("MAP LAYER", features.error ?: "No message")
        }
    }

    // 3B. Build selection of Parking
    private fun buildParkingSelection(features: Expected<String, MutableList<QueriedFeature>>) {
        if (features.isValue) {
            features.value?.firstOrNull()?.feature?.let {
                if (it.hasProperty("name") &&
                    it.hasProperty("googleUrl")
                ) {
                    selectPois(
                        it.getStringProperty("name"),
                        it.getStringProperty("googleUrl"),
                        it.geometry()
                    )
                }
            }
        } else {
            Log.w("MAP LAYERS", features.error ?: "No message")
        }
    }

    // 3C. Build selection of Problems
    private fun buildProblemsSelection(features: Expected<String, MutableList<QueriedFeature>>, y: Double) {
        if (features.isValue) {
            println("PROBLEMS ${features.value}")
            features.value?.firstOrNull()?.feature?.let {
                println("PROBLEM $it")
                if (it.hasProperty("id") && it.geometry() != null) {
                    selectProblem(it.getNumberProperty("id").toInt())

                    println("Will be hidden ${y >= binding.mapView.height / 2}")
                    // Move camera is problem is hidden by bottomSheet
                    if (y >= binding.mapView.height / 2) {

                        val cameraOption = CameraOptions.Builder()
                            .center(it.geometry() as Point)
                            .padding(EdgeInsets(60.0, 8.8, (binding.root.height / 2).toDouble(), 8.8))
                            .build()
                        val mapAnimationOption = MapAnimationOptions.Builder()
                            .duration(500L)
                            .build()

                        binding.mapView.getMapboxMap().easeTo(cameraOption, mapAnimationOption)
                    }
                }
            }
        } else {
            Log.w("MAP LAYERS", features.error ?: "No message")
        }
    }

    // 4A. Move the camera boxed to specific coordinates
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

    // 4B.
    private fun selectPois(name: String, googleUrl: String, geometry: Geometry?) {

    }

    // 4C.
    private fun selectProblem(problemId: Int) {
        //TODO REMOVE
        lifecycleScope.launch {
            mainViewModel.getProblemById(2102).collect {
                println("RESULT COUNT ${it.size}\n FIRST RESULT ${it.firstOrNull()?.id}")
            }
        }
    }
}