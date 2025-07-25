package com.boolder.boolder.view.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import androidx.core.graphics.Insets
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.BoolderMapConfig
import com.boolder.boolder.domain.model.Circuit
import com.boolder.boolder.domain.model.TopoOrigin
import com.boolder.boolder.utils.MapboxStyleFactory.Companion.LAYER_CIRCUITS
import com.boolder.boolder.utils.MapboxStyleFactory.Companion.LAYER_CIRCUIT_PROBLEMS
import com.boolder.boolder.utils.MapboxStyleFactory.Companion.LAYER_CIRCUIT_PROBLEMS_TEXT
import com.boolder.boolder.utils.MapboxStyleFactory.Companion.LAYER_PROBLEMS
import com.boolder.boolder.utils.MapboxStyleFactory.Companion.LAYER_PROBLEMS_NAMES
import com.boolder.boolder.utils.MapboxStyleFactory.Companion.LAYER_PROBLEMS_NAMES_ANTI_OVERLAP
import com.boolder.boolder.utils.MapboxStyleFactory.Companion.LAYER_PROBLEMS_TEXT
import com.boolder.boolder.utils.extension.coerceZoomAtLeast
import com.boolder.boolder.view.map.animator.animationEndListener
import com.boolder.boolder.view.map.extension.getAreaBarAndFiltersHeight
import com.boolder.boolder.view.map.extension.getAreaBarHeight
import com.boolder.boolder.view.map.extension.getCircuitStartButtonHeight
import com.boolder.boolder.view.map.extension.getDefaultMargin
import com.boolder.boolder.view.map.extension.getTopoBottomSheetHeight
import com.boolder.boolder.view.map.extension.getTopoBottomSheetHeightWithMargin
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.QueriedRenderedFeature
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.ScreenBox
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.style.StyleContract.StyleExtension
import com.mapbox.maps.extension.style.expressions.dsl.generated.all
import com.mapbox.maps.extension.style.expressions.dsl.generated.match
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.getLayerAs
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.attribution.AttributionPlugin
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar

@SuppressLint("ViewConstructor")
class BoolderMap(
    context: Context,
    mapInitOptions: MapInitOptions
) : MapView(context, mapInitOptions) {

    companion object {
        private const val TAG = "Boolder Map"

        private const val CAMERA_CHECK_THROTTLE_DELAY = 100L
    }

    interface BoolderMapListener {
        fun onProblemSelected(problemId: Int, origin: TopoOrigin)
        fun onTopoUnselected()
        fun onPoiSelected(poiName: String, googleMapsUrl: String)

        fun onAreaVisited(areaId: Int)
        fun onAreaLeft()
        fun onZoomLevelChanged(zoomLevel: Double)
    }

    private var listener: BoolderMapListener? = null

    private var previousSelectedFeatureId: String? = null

    private var lastCameraCheckTimestamp = 0L

    private var insets = Insets.NONE

    init {
        gestures.pitchEnabled = false
        scalebar.enabled = false
        compass.updateSettings {
            val compassMargin = resources.getDimension(R.dimen.margin_map_controls)

            visibility = true
            position = Gravity.BOTTOM or Gravity.START
            marginLeft = compassMargin
            marginRight = compassMargin
            marginBottom = compassMargin
        }
        addClickEvent()

        mapboxMap.subscribeCameraChanged {
            val now = System.currentTimeMillis()

            if (now - lastCameraCheckTimestamp < CAMERA_CHECK_THROTTLE_DELAY) return@subscribeCameraChanged

            lastCameraCheckTimestamp = now

            detectArea()
        }

        camera.addCameraZoomChangeListener { listener?.onZoomLevelChanged(it) }
    }

    fun setup(listener: BoolderMapListener, buildStyle: StyleExtension) {
        this.listener = listener

        getPlugin<AttributionPlugin>(Plugin.MAPBOX_ATTRIBUTION_PLUGIN_ID)
            ?.getMapAttributionDelegate()
            ?.telemetry()
            ?.apply {
                disableTelemetrySession()
                userTelemetryRequestState = false
            }

        mapboxMap.loadStyle(buildStyle)
        location.locationPuck = createDefault2DPuck(withBearing = true)
        location.puckBearingEnabled = true
        location.puckBearing = PuckBearing.HEADING
    }

    private fun addClickEvent() {
        mapboxMap.addOnMapClickListener {
            val event = mapboxMap.pixelForCoordinate(it)
            installRenderedFeatures(event.x, event.y)
            true
        }
    }

    // 2. Use x and y to determine whether or not it is relevant
    private fun installRenderedFeatures(x: Double, y: Double) {
        val geometry = RenderedQueryGeometry(
            ScreenCoordinate(x, y)
        )
        mapboxMap.apply {
            queryAreaRenderedFeatures(geometry)
            queryClusterRenderedFeatures(geometry)
            queryPoisRenderedFeatures(geometry)
            queryProblemRenderedFeatures(geometry)
        }
    }

    private fun queryAreaRenderedFeatures(geometry: RenderedQueryGeometry) {
        val areaOption = RenderedQueryOptions(
            listOf("areas", "areas-hulls"),
            Expression.lt(
                Expression.zoom(),
                Expression.literal(15.0)
            )
        )
        mapboxMap.queryRenderedFeatures(
            geometry,
            areaOption
        ) { features: Expected<String, List<QueriedRenderedFeature>> ->
            buildCoordinateBounds(features)
        }
    }

    private fun queryClusterRenderedFeatures(geometry: RenderedQueryGeometry) {
        val clusterOption = RenderedQueryOptions(listOf("clusters"), null)

        mapboxMap.queryRenderedFeatures(
            geometry,
            clusterOption
        ) { features: Expected<String, List<QueriedRenderedFeature>> ->
            buildCoordinateBounds(features)
        }
    }

    private fun queryProblemRenderedFeatures(geometry: RenderedQueryGeometry) {

        val tapSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 30f, resources.displayMetrics)
        val problemGeometry = RenderedQueryGeometry(
            ScreenBox(
                ScreenCoordinate(
                    geometry.screenCoordinate.x - tapSize/2,
                    geometry.screenCoordinate.y - tapSize/2
                ),
                ScreenCoordinate(
                    geometry.screenCoordinate.x + tapSize/2,
                    geometry.screenCoordinate.y + tapSize/2
                )
            )
        )

        val problemsOption = RenderedQueryOptions(
            listOf(LAYER_PROBLEMS, LAYER_CIRCUIT_PROBLEMS, LAYER_PROBLEMS_NAMES),
            null
        )

        mapboxMap.queryRenderedFeatures(
            problemGeometry,
            problemsOption
        ) { features: Expected<String, List<QueriedRenderedFeature>> ->
            if (features.isValue) {
                val feature = features.value?.firstOrNull()?.queriedFeature?.feature
                    ?: run {
                        unselectProblem()
                        listener?.onTopoUnselected()
                        return@queryRenderedFeatures
                    }

                if (mapboxMap.cameraState.zoom < 19.0) {
                    zoomToBoulderProblemLevel(feature)
                    return@queryRenderedFeatures
                }

                if (feature.hasProperty("id") && feature.geometry() != null) {
                    selectProblem(feature.getNumberProperty("id").toString())

                    // Move camera if problem is hidden by bottomSheet
                    val yThreshold = height - resources.getTopoBottomSheetHeightWithMargin()

                    if (geometry.screenCoordinate.y >= yThreshold) {
                        focusOnBoulderProblem(feature)
                    } else {
                        listener?.onProblemSelected(
                            problemId = feature.getNumberProperty("id").toInt(),
                            origin = TopoOrigin.MAP
                        )
                    }
                } else {
                    unselectProblem()
                }
            } else {
                Log.w(TAG, features.error ?: "No message")
            }
        }
    }

    private fun queryPoisRenderedFeatures(
        geometry: RenderedQueryGeometry,
    ) {
        val poisOption = RenderedQueryOptions(listOf("pois"), null)
        mapboxMap.queryRenderedFeatures(
            geometry,
            poisOption
        ) { features: Expected<String, List<QueriedRenderedFeature>> ->
            if (mapboxMap.cameraState.zoom < 12) return@queryRenderedFeatures
            if (features.isValue) {
                features.value?.firstOrNull()?.queriedFeature?.feature?.let {
                    if (it.hasProperty("name") && it.hasProperty("googleUrl")) {
                        listener?.onPoiSelected(
                            poiName = it.getStringProperty("name"),
                            googleMapsUrl = it.getStringProperty("googleUrl")
                        )
                    }
                }
            } else {
                Log.w(TAG, features.error ?: "No message")
            }
        }
    }

    fun selectProblem(featureId: String) {
        mapboxMap.setFeatureState(
            sourceId = "problems",
            sourceLayerId = BoolderMapConfig.problemsSourceLayerId,
            featureId = featureId,
            state = Value(hashMapOf("selected" to Value(true))),
            callback = {
                if (it.isError) return@setFeatureState

                if (featureId != previousSelectedFeatureId) unselectProblem()

                previousSelectedFeatureId = featureId
            }
        )
    }

    private fun unselectProblem() {
        previousSelectedFeatureId?.let {
            mapboxMap.setFeatureState(
                sourceId = "problems",
                sourceLayerId = BoolderMapConfig.problemsSourceLayerId,
                featureId = it,
                state = Value(hashMapOf("selected" to Value(false))),
                callback = {}
            )
        }
    }

    fun updateCircuit(circuitId: Long?) {
        circuitId ?: run {
            hideCircuit()
            return
        }

        getLayerAs<LineLayer>(LAYER_CIRCUITS)?.apply {
            filter(
                match {
                    get("id")
                    literal(circuitId)
                    literal(true)
                    literal(false)
                }
            )
            visibility(Visibility.VISIBLE)
        }

        getLayerAs<CircleLayer>(LAYER_CIRCUIT_PROBLEMS)?.apply {
            filter(
                match {
                    get("circuitId")
                    literal(circuitId)
                    literal(true)
                    literal(false)
                }
            )
            visibility(Visibility.VISIBLE)
        }

        getLayerAs<SymbolLayer>(LAYER_CIRCUIT_PROBLEMS_TEXT)?.apply {
            filter(
                match {
                    get("circuitId")
                    literal(circuitId)
                    literal(true)
                    literal(false)
                }
            )
            visibility(Visibility.VISIBLE)
        }
    }

    private fun hideCircuit() {
        val layersToHide = listOfNotNull(
            getLayerAs<LineLayer>(LAYER_CIRCUITS),
            getLayerAs<CircleLayer>(LAYER_CIRCUIT_PROBLEMS),
            getLayerAs<SymbolLayer>(LAYER_CIRCUIT_PROBLEMS_TEXT)
        )

        layersToHide.forEach { it.visibility(Visibility.NONE) }
    }

    // 3A. Build bounds around coordinate
    private fun buildCoordinateBounds(
        features: Expected<String, List<QueriedRenderedFeature>>
    ) {
        if (features.isValue) {
            features.value?.firstOrNull()?.queriedFeature?.feature?.let {
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

                    val areaId = if (it.hasProperty("areaId")) {
                        it.getStringProperty("areaId").toInt()
                    } else {
                        null
                    }

                    zoomToAreaBounds(
                        coordinates = listOf(southWest, northEst),
                        areaId = areaId
                    )
                }
            } ?: unselectProblem()
        } else {
            Log.w(TAG, features.error ?: "No message")
        }
    }

    // Triggered when user click on a Area or Cluster on Map
    fun zoomToAreaBounds(
        coordinates: List<Point>,
        areaId: Int?
    ) {
        val topInset = insets.top + resources.getAreaBarAndFiltersHeight().toDouble()
        val defaultInset = resources.getDefaultMargin().toDouble()

        mapboxMap.cameraForCoordinates(
            coordinates = coordinates,
            camera = CameraOptions.Builder().build(),
            coordinatesPadding = EdgeInsets(topInset, defaultInset, defaultInset, defaultInset),
            maxZoom = null,
            offset = null
        ) { cameraOptions ->
            val mapAnimationOption = MapAnimationOptions.mapAnimationOptions { duration(500L) }

            mapboxMap.flyTo(
                cameraOptions = cameraOptions,
                animationOptions = mapAnimationOption,
                animatorListener = animationEndListener {
                    areaId ?: return@animationEndListener

                    listener?.onAreaVisited(areaId)
                }
            )
        }
    }

    private fun zoomToCircuitBounds(circuitCoordinates: List<Point>) {
        val topInset = insets.top + resources.getAreaBarAndFiltersHeight().toDouble()
        val bottomInset = resources.getCircuitStartButtonHeight().toDouble()
        val defaultInset = resources.getDefaultMargin().toDouble()

        mapboxMap.cameraForCoordinates(
            coordinates = circuitCoordinates,
            camera = CameraOptions.Builder().build(),
            coordinatesPadding = EdgeInsets(topInset, defaultInset, bottomInset, defaultInset),
            maxZoom = null,
            offset = null
        ) { cameraOptions ->
            val mapAnimationOption = MapAnimationOptions.mapAnimationOptions { duration(500L) }

            mapboxMap.flyTo(cameraOptions.coerceZoomAtLeast(15.0), mapAnimationOption)
        }
    }


    private fun zoomToBoulderProblemLevel(feature: Feature) {
        val cameraOptions = CameraOptions.Builder()
            .center(feature.geometry() as Point)
            .padding(EdgeInsets(0.0, 0.0,0.0, 0.0))
            .zoom(19.0)
            .build()

        val mapAnimationOption = MapAnimationOptions.mapAnimationOptions { duration(500L) }

        mapboxMap.easeTo(cameraOptions, mapAnimationOption)
    }

    private fun focusOnBoulderProblem(feature: Feature) {
        val topInset = insets.top + resources.getAreaBarHeight().toDouble()
        val bottomInset = resources.getTopoBottomSheetHeight().toDouble()

        val cameraOptions = CameraOptions.Builder()
            .center(feature.geometry() as Point)
            .padding(EdgeInsets(topInset, 0.0, bottomInset, 0.0))
            .build()

        val mapAnimationOption = MapAnimationOptions.mapAnimationOptions { duration(500L) }

        mapboxMap.easeTo(
            cameraOptions = cameraOptions,
            animationOptions = mapAnimationOption,
            animatorListener = animationEndListener {
                listener?.onProblemSelected(
                    problemId = feature.getNumberProperty("id").toInt(),
                    origin = TopoOrigin.MAP
                )
            }
        )
    }

    fun applyInsets(insets: Insets) {
        this.insets = insets

        val bottomNavHeight = resources.getDimensionPixelSize(R.dimen.height_bottom_nav_bar)

        updateLayoutParams<MarginLayoutParams> {
            updateMargins(bottom = insets.bottom + bottomNavHeight)
        }
    }

    fun onCircuitSelected(circuit: Circuit) {
        zoomToCircuitBounds(circuitCoordinates = circuit.coordinateBounds)
    }

    private inline fun <reified T : Layer> getLayerAs(layerId: String): T? =
        mapboxMap.style?.getLayerAs(layerId)

    fun applyFilters(
        grades: List<String>,
        showPopular: Boolean,
        projectIds: List<Int>,
        tickedIds: List<Int>
    ) {
        val problemsLayer = getLayerAs<CircleLayer>(LAYER_PROBLEMS)
        val problemsTextLayer = getLayerAs<SymbolLayer>(LAYER_PROBLEMS_TEXT)

        val popularLayer = getLayerAs<SymbolLayer>(LAYER_PROBLEMS_NAMES)
        val popularAntiOverlapLayer = getLayerAs<SymbolLayer>(LAYER_PROBLEMS_NAMES_ANTI_OVERLAP)

        val showProjects = projectIds.isNotEmpty()
        val showTicked = tickedIds.isNotEmpty()

        val query = all {
            match {
                get("grade")
                literal(grades)
                literal(true)
                literal(false)
            }

            if (showPopular) get("featured")

            if (showProjects) {
                inExpression {
                    get("id")
                    literal(projectIds)
                }
            }

            if (showTicked) {
                inExpression {
                    get("id")
                    literal(tickedIds)
                }
            }
        }

        problemsLayer?.filter(query)
        problemsTextLayer?.filter(query)

        fun SymbolLayer.update() = apply {
            filter(query)
            visibility(
                if (showPopular || showProjects || showTicked) {
                    Visibility.VISIBLE
                } else {
                    Visibility.NONE
                }
            )
        }

        popularLayer?.update()
        popularAntiOverlapLayer?.update()
    }

    fun detectArea() {
        val halfSquareSize = width / 8

        val left = (width / 2 - halfSquareSize).toDouble()
        val right = (width / 2 + halfSquareSize).toDouble()
        val top = (height / 2 - halfSquareSize).toDouble()
        val bottom = (height / 2 + halfSquareSize).toDouble()

        mapboxMap.queryRenderedFeatures(
            geometry = RenderedQueryGeometry(
                listOf(
                    ScreenCoordinate(left, top),
                    ScreenCoordinate(right, top),
                    ScreenCoordinate(right, bottom),
                    ScreenCoordinate(left, bottom)
                )
            ),
            options = RenderedQueryOptions(
                listOf("areas-hulls"),
                Expression.gt {
                    zoom()
                    literal(14.5)
                }
            )
        ) { queriedFeature ->
            queriedFeature.value?.firstOrNull()?.queriedFeature?.feature?.properties()?.get("areaId")
                ?.let { areaId -> listener?.onAreaVisited(areaId.asInt) }
                ?: listener?.onAreaLeft()
        }
    }
}
