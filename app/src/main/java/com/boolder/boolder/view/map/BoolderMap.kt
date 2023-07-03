package com.boolder.boolder.view.map

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import com.boolder.boolder.R
import com.boolder.boolder.domain.model.BoolderMapConfig
import com.boolder.boolder.domain.model.Problem
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.Value
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
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.animation.flyTo
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
    }

    interface BoolderClickListener {
        fun onProblemSelected(problemId: Int)
        fun onPoisSelected(poisName: String, stringProperty: String, geometry: Geometry?)
    }

    private var listener: BoolderClickListener? = null

    private var previousSelectedFeatureId: String? = null

    init {
        init()
    }

    fun setup(listener: BoolderClickListener, buildStyle: StyleExtension) {
        this.listener = listener
        getMapboxMap().loadStyle(buildStyle)
    }

    private fun init() {
        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(2.5968216, 48.3925623))
            .zoom(10.2)
            .build()

        getMapboxMap().apply {
            setCamera(cameraOptions)
        }

        gestures.pitchEnabled = false
        scalebar.enabled = false
        compass.visibility = true
        compass.marginTop = resources.getDimension(R.dimen.margin_compass_top)
        compass.marginRight = resources.getDimension(R.dimen.margin_compass_end)
        addClickEvent()
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

        val problemsOption = RenderedQueryOptions(listOf("problems"), null)

        getMapboxMap().queryRenderedFeatures(
            problemGeometry,
            problemsOption
        ) { features: Expected<String, MutableList<QueriedFeature>> ->
            if (features.isValue) {

                val feature = features.value?.firstOrNull()?.feature ?: return@queryRenderedFeatures
                if (getMapboxMap().cameraState.zoom < 18) return@queryRenderedFeatures
                if (feature.hasProperty("id") && feature.geometry() != null) {
                    selectProblem(feature.getNumberProperty("id").toString())
                    listener?.onProblemSelected(feature.getNumberProperty("id").toInt())

                    // Move camera is problem is hidden by bottomSheet
                    if (geometry.screenCoordinate.y >= (height / 2) - 100) {

                        val cameraOption = CameraOptions.Builder()
                            .center(feature.geometry() as Point)
                            .padding(EdgeInsets(40.0, 8.8, (height / 2).toDouble(), 8.8))
                            .build()
                        val mapAnimationOption = MapAnimationOptions.Builder()
                            .duration(500L)
                            .build()

                        getMapboxMap().easeTo(cameraOption, mapAnimationOption)
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

    fun selectProblemAndCenter(problem: Problem) {
        selectProblem(problem.id.toString())
        val point = Point.fromLngLat(
            problem.longitude.toDouble(),
            problem.latitude.toDouble()
        )

        val coordinates = getMapboxMap().pixelForCoordinate(point)

        // Move camera is problem is hidden by bottomSheet
        if (coordinates.y >= height / 2) {

            val cameraOption = CameraOptions.Builder()
                .center(point)
                .padding(EdgeInsets(40.0, 8.8, (height / 2).toDouble(), 8.8))
                .build()
            val mapAnimationOption = MapAnimationOptions.Builder()
                .duration(500L)
                .build()

            getMapboxMap().easeTo(cameraOption, mapAnimationOption)
        }
    }

    private fun unselectProblem() {
        previousSelectedFeatureId?.let {
            getMapboxMap().setFeatureState(
                "problems",
                BoolderMapConfig.problemsSourceLayerId,
                it,
                Value.fromJson("""{"selected": false}""").value!!
            )
        }
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
                    moveCamera(coordinateBound)
                }
            } ?: unselectProblem()
        } else {
            Log.w(TAG, features.error ?: "No message")
        }
    }

    // Triggered when user click on a Area or Cluster on Map
    private fun moveCamera(coordinates: CoordinateBounds) {
        val cameraOption = getMapboxMap().cameraForCoordinateBounds(
            coordinates,
            EdgeInsets(60.0, 8.0, 8.0, 8.0),
            0.0,
            0.0
        )

        val mapAnimationOption = MapAnimationOptions.Builder()
            .duration(500L)
            .build()

        getMapboxMap().flyTo(cameraOption, mapAnimationOption)
    }

    fun applyCompassTopInset(topInset: Float) {
        compass.marginTop = resources.getDimension(R.dimen.margin_compass_top) + topInset
    }
}