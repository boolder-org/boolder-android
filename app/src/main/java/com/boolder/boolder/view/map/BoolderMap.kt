package com.boolder.boolder.view.map

import android.content.Context
import android.util.AttributeSet
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
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.QueriedFeature
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
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.attribution.AttributionPlugin
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar

class BoolderMap @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MapView(context, attrs, defStyle) {

    companion object {
        private const val TAG = "Boolder Map"

        private const val CAMERA_CHECK_THROTTLE_DELAY = 100L
    }

    interface BoolderMapListener {
        fun onProblemSelected(problemId: Int, origin: TopoOrigin)
        fun onTopoUnselected()
        fun onPoisSelected(poisName: String, stringProperty: String, geometry: Geometry?)

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

        getMapboxMap().addOnCameraChangeListener {
            val now = System.currentTimeMillis()

            if (now - lastCameraCheckTimestamp < CAMERA_CHECK_THROTTLE_DELAY) return@addOnCameraChangeListener

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

        getMapboxMap().loadStyle(buildStyle)
    }

    private fun addClickEvent() {
        getMapboxMap().addOnMapClickListener {
            val event = getMapboxMap().pixelForCoordinate(it)
            installRenderedFeatures(event.x, event.y)
            true
        }
    }

    // 2. Use x and y to determine whether or not it is relevant
    private fun installRenderedFeatures(x: Double, y: Double) {
        val geometry = RenderedQueryGeometry(
            ScreenCoordinate(x, y)
        )
        getMapboxMap().apply {
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
        getMapboxMap().queryRenderedFeatures(
            geometry,
            areaOption
        ) { features: Expected<String, MutableList<QueriedFeature>> ->
            buildCoordinateBounds(features)
        }
    }

    private fun queryClusterRenderedFeatures(geometry: RenderedQueryGeometry) {
        val clusterOption = RenderedQueryOptions(listOf("clusters"), null)

        getMapboxMap().queryRenderedFeatures(
            geometry,
            clusterOption
        ) { features: Expected<String, MutableList<QueriedFeature>> ->
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

        getMapboxMap().queryRenderedFeatures(
            problemGeometry,
            problemsOption
        ) { features: Expected<String, MutableList<QueriedFeature>> ->
            if (features.isValue) {
                val feature = features.value?.firstOrNull()?.feature
                    ?: run {
                        unselectProblem(notifyListener = true)
                        return@queryRenderedFeatures
                    }

                if (getMapboxMap().cameraState.zoom < 19.0) {
                    zoomToBoulderProblemLevel(feature)
                    return@queryRenderedFeatures
                }

                if (feature.hasProperty("id") && feature.geometry() != null) {
                    selectProblem(feature.getNumberProperty("id").toString())
                    listener?.onProblemSelected(
                        problemId = feature.getNumberProperty("id").toInt(),
                        origin = TopoOrigin.MAP
                    )

                    // Move camera if problem is hidden by bottomSheet
                    if (geometry.screenCoordinate.y >= (height / 2) - 100) {
                        focusOnBoulderProblem(feature)
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
        getMapboxMap().queryRenderedFeatures(
            geometry,
            poisOption
        ) { features: Expected<String, MutableList<QueriedFeature>> ->
            if (getMapboxMap().cameraState.zoom < 12) return@queryRenderedFeatures
            if (features.isValue) {
                features.value?.firstOrNull()?.feature?.let {
                    if (it.hasProperty("name") &&
                        it.hasProperty("googleUrl")
                    ) {
                        listener?.onPoisSelected(
                            it.getStringProperty("name"),
                            it.getStringProperty("googleUrl"),
                            it.geometry()
                        )
                    }
                }
            } else {
                Log.w(TAG, features.error ?: "No message")
            }
        }
    }

    fun selectProblem(featureId: String) {
        getMapboxMap().setFeatureState(
            "problems",
            BoolderMapConfig.problemsSourceLayerId,
            featureId,
            Value.fromJson("""{"selected": true}""").value!!
        )

        if (featureId != previousSelectedFeatureId) {
            unselectProblem()
        }
        previousSelectedFeatureId = featureId
    }

    fun unselectProblem(notifyListener: Boolean = false) {
        val previousSelectedFeatureId = previousSelectedFeatureId ?: return
        getMapboxMap().setFeatureState(
            "problems",
            BoolderMapConfig.problemsSourceLayerId,
            previousSelectedFeatureId,
            Value.fromJson("""{"selected": false}""").value!!
        )
        if (notifyListener) listener?.onTopoUnselected()
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
        features: Expected<String, MutableList<QueriedFeature>>
    ) {
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

                    val areaId = if (it.hasProperty("areaId")) {
                        it.getStringProperty("areaId").toInt()
                    } else {
                        null
                    }

                    zoomToAreaBounds(coordinates = coordinateBound, areaId = areaId)
                }
            } ?: unselectProblem()
        } else {
            Log.w(TAG, features.error ?: "No message")
        }
    }

    // Triggered when user click on a Area or Cluster on Map
    private fun zoomToAreaBounds(
        coordinates: CoordinateBounds,
        areaId: Int?
    ) {
        val cameraOptions = getMapboxMap().cameraForCoordinateBounds(
            coordinates,
            EdgeInsets(60.0, 8.0, 8.0, 8.0),
            0.0,
            0.0
        )

        val mapAnimationOption = MapAnimationOptions.mapAnimationOptions {
            duration(500L)

            areaId ?: return@mapAnimationOptions

            animatorListener(animationEndListener { listener?.onAreaVisited(areaId) })
        }

        getMapboxMap().flyTo(cameraOptions, mapAnimationOption)
    }

    private fun zoomToCircuitBounds(circuitCoordinates: CoordinateBounds) {
        val defaultMarginPixels = 24.0 * resources.displayMetrics.density
        val cameraOptions = getMapboxMap().cameraForCoordinateBounds(
            circuitCoordinates,
            EdgeInsets(
                140.0 * resources.displayMetrics.density + insets.top,
                defaultMarginPixels,
                88.0 * resources.displayMetrics.density + insets.bottom,
                defaultMarginPixels
            ),
            0.0,
            0.0
        ).coerceZoomAtLeast(15.0)

        val mapAnimationOption = MapAnimationOptions.mapAnimationOptions {
            duration(500L)
        }

        getMapboxMap().flyTo(cameraOptions, mapAnimationOption)
    }


    private fun zoomToBoulderProblemLevel(feature: Feature) {
        val cameraOptions = CameraOptions.Builder()
            .center(feature.geometry() as Point)
            .padding(EdgeInsets(0.0, 0.0,0.0, 0.0))
            .zoom(19.0)
            .build()

        val mapAnimationOption = MapAnimationOptions.mapAnimationOptions { duration(500L) }

        getMapboxMap().easeTo(cameraOptions, mapAnimationOption)
    }

    private fun focusOnBoulderProblem(feature: Feature) {
        val cameraOptions = CameraOptions.Builder()
            .center(feature.geometry() as Point)
            .padding(EdgeInsets(40.0, 8.8, (height / 2).toDouble(), 8.8))
            .build()

        val mapAnimationOption = MapAnimationOptions.mapAnimationOptions { duration(500L) }

        getMapboxMap().easeTo(cameraOptions, mapAnimationOption)
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
        getMapboxMap().getStyle()?.getLayerAs(layerId)

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

        getMapboxMap().queryRenderedFeatures(
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
            queriedFeature.value?.firstOrNull()?.feature?.properties()?.get("areaId")
                ?.let { areaId -> listener?.onAreaVisited(areaId.asInt) }
                ?: listener?.onAreaLeft()
        }
    }
}
