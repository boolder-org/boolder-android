package com.boolder.boolder.view.detail

import android.content.Context
import android.graphics.*
import android.graphics.Path.Direction.CW
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View


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
//    private val borderPath = Path()
//    private val pathPaint = Paint()
//    private val borderPathPaint = Paint()

    private val crtl1PointPath = Path()
    private val crtl2PointPath = Path()
    private val stopPoint = Path()
    private val linePath = Path()

    private val crtl1Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 3f
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val crtl2Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 3f
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val borderPathWidth by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
        )
    }

    private val stopPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = borderPathWidth + borderPathWidth
        style = Paint.Style.FILL
        color = Color.BLUE
    }

    private val linePaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = borderPathWidth
        style = Paint.Style.STROKE
        color = Color.RED
    }


    private var viewCanvas: Canvas? = null
    private var bitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)

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

            linePath.reset()
            linePath.moveTo(points.first().x, points.first().y)

            points.forEach {
                stopPoint.addCircle(it.x, it.y, 10f, CW)
            }
            for (i in 1 until points.size) {
                println("POINT ${points[i]}")
                linePath.cubicTo(
                    conPoint1[i - 1].x, conPoint1[i - 1].y, conPoint2[i - 1].x, conPoint2[i - 1].y,
                    points[i].x, points[i].y
                )
            }

            canvas?.drawPath(stopPoint, stopPaint)
            canvas?.drawPath(linePath, linePaint)

        } catch (e: Exception) {
            Log.e("TAG", e.message ?: "")
        }
    }

    fun addDataPoints(data: List<PointD>, point1: List<PointD>, point2: List<PointD>) {
        post {
            Thread(Runnable {

                points.addAll(data.toF())
                conPoint1.addAll(point1.toF())
                conPoint2.addAll(point2.toF())

                postInvalidate()
                return@Runnable

            }).start()
        }
    }
}