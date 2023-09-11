package com.boolder.boolder.view.detail

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.boolder.boolder.R


class ProblemLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val points = mutableListOf<PointF>()
    private val controlPoint1 = mutableListOf<PointF>()
    private val controlPoint2 = mutableListOf<PointF>()

    private val path = Path()

    private val lineTrickiness by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
        )
    }

    private val paint = Paint().apply {
        setShadowLayer(
            resources.getDimension(R.dimen.radius_problem_line),
            0f,
            0f,
            ContextCompat.getColor(context, R.color.problem_line_shadow)
        )
        setLayerType(LAYER_TYPE_SOFTWARE, this)
    }

    private var lineLengthRatio = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawProblemPath()
    }

    private fun Canvas.drawProblemPath() {
        try {
            if (points.isEmpty() && controlPoint1.isEmpty() && controlPoint2.isEmpty()) return

            path.reset()
            path.moveTo(points.first().x * width, points.first().y * height)

            for (i in 1 until points.size) {
                path.cubicTo(
                    controlPoint1[i.dec()].x * width,
                    controlPoint1[i.dec()].y * height,
                    controlPoint2[i.dec()].x * width,
                    controlPoint2[i.dec()].y * height,
                    points[i].x * width,
                    points[i].y * height
                )
            }

            val pathMeasure = PathMeasure(path, false)
            val length = pathMeasure.length

            paint.pathEffect = DashPathEffect(
                floatArrayOf(
                    length * lineLengthRatio,
                    length * 1f
                ),
                0f
            )

            drawPath(path, paint)

        } catch (e: Exception) {
            Log.e("TAG", e.message ?: "")
        }
    }

    fun addDataPoints(
        data: List<PointD>,
        point1: List<PointD>,
        point2: List<PointD>,
        @ColorInt drawColor: Int
    ) {

        paint.apply {
            isAntiAlias = true
            strokeWidth = lineTrickiness
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = drawColor
        }

        post {
            Thread {
                points.clear()
                controlPoint1.clear()
                controlPoint2.clear()

                points.addAll(data.toPointFList())
                controlPoint1.addAll(point1.toPointFList())
                controlPoint2.addAll(point2.toPointFList())

                postInvalidate()

            }.start()
        }
    }

    fun clearPath() {
        lineLengthRatio = 0f
        points.clear()
        controlPoint1.clear()
        controlPoint2.clear()
        postInvalidate()
    }

    fun animatePath() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            startDelay = 300L
            duration = 400L

            addUpdateListener { valueAnimator ->
                lineLengthRatio = valueAnimator.animatedFraction
                postInvalidate()
            }

            start()
        }
    }
}

data class PointD(val x: Double, val y: Double)

fun List<PointD>.toPointFList() = map { PointF(it.x.toFloat(), it.y.toFloat()) }
