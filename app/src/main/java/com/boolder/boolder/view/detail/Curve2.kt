package com.boolder.boolder.view.detail

import android.content.Context
import android.graphics.*
import android.graphics.Path.Direction.CW
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat


class RallyLineGraphChart @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val data = mutableListOf<PointD>()
    private val points = mutableListOf<PointF>()
    private val conPoint1 = mutableListOf<PointF>()
    private val conPoint2 = mutableListOf<PointF>()

    private val path = Path()
    private val borderPath = Path()
    private val barPath = Path()
    private val pathPaint = Paint()
    private val borderPathPaint = Paint()
    private val barPaint = Paint()

    private var viewCanvas: Canvas? = null
    private var bitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)

    private val borderPathWidth by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
        )
    }

    init {
        init(attrs)
    }

    private fun init(set: AttributeSet?) {
        borderPathPaint.apply {
            isAntiAlias = true
            strokeWidth = borderPathWidth
            style = Paint.Style.STROKE
            color = Color.RED
        }

        pathPaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.BLACK
        }
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(w, h, oldw, oldh)

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        viewCanvas = Canvas(bitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawBezierCurve(canvas)
        bitmap?.let {
            canvas?.drawBitmap(it, 0f, 0f, bitmapPaint)
        }
    }

    private fun drawBezierCurve(canvas: Canvas?) {

        try {

            if (points.isEmpty() && conPoint1.isEmpty() && conPoint2.isEmpty()) return

            path.reset()
            path.moveTo(points.first().x, points.first().y)
            points.forEach {
                path.addCircle(it.x, it.y, 10f, CW)
            }
            conPoint1.forEach {
                path.addCircle(it.x, it.y, 10f, CW)
            }

            conPoint2.forEach {
                path.addCircle(it.x, it.y, 10f, CW)
            }


            for (i in 1 until points.size) {
                path.cubicTo(
                    conPoint1[i - 1].x, conPoint1[i - 1].y, conPoint2[i - 1].x, conPoint2[i - 1].y,
                    points[i].x, points[i].y
                )
            }

            borderPath.set(path)


            canvas?.drawPath(borderPath, borderPathPaint)

        } catch (e: Exception) {
        }
    }

    private fun getLargeBarHeight() = height / 3 * 2f

    fun addDataPoints(data: List<PointD>, point1: List<PointD>, point2: List<PointD>) {
        //do calculation in worker thread // Note: You should use some safe thread mechanism
        //Calculation logic here are not fine, should updated when more time available
        post {
            Thread(Runnable {

                println("HEIGHT $height")
                println("WIDTH $width")
                points.addAll(data.toF())
                conPoint1.addAll(point1.toF())
                conPoint2.addAll(point2.toF())

                postInvalidate()
                return@Runnable

            }).start()
        }
    }

    private fun resetDataPoints() {
        this.data.clear()
        points.clear()
        conPoint1.clear()
        conPoint2.clear()
    }

    fun setCurveBorderColor(@ColorRes color: Int) {
        borderPathPaint.color = ContextCompat.getColor(context, color)
    }

    companion object {
        private const val INDEX_OF_LARGE_BAR = 8
        private const val VERTICAL_BARS =
            (INDEX_OF_LARGE_BAR * INDEX_OF_LARGE_BAR) + 6 // add fixed bars size
        private const val CURVE_BOTTOM_MARGIN = 32f

    }
}

data class DataPoint(val amount: Float)