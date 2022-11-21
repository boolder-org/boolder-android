package com.boolder.boolder.view.detail

import android.content.Context
import android.graphics.*
import android.graphics.Path.Direction.CW
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import com.boolder.boolder.R


class RallyLineGraphChart @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val data = mutableListOf<PointD>()
    private val points = mutableListOf<PointF>()
    private val conPoint1 = mutableListOf<PointF>()
    private val conPoint2 = mutableListOf<PointF>()

    private val stopPath = Path()
    private val linePath = Path()

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

    private val linePaint = Paint()

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
        val textPaint = Paint().apply {
            color = context.getColor(R.color.white)
            textSize = 28f
        }

        try {
            if (points.isEmpty() && conPoint1.isEmpty() && conPoint2.isEmpty()) return

            linePath.reset()
            linePath.moveTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                linePath.cubicTo(
                    conPoint1[i.dec()].x,
                    conPoint1[i.dec()].y,
                    conPoint2[i.dec()].x,
                    conPoint2[i.dec()].y,
                    points[i].x,
                    points[i].y
                )
            }

            canvas?.drawPath(linePath, linePaint)

            stopPath.reset()
            stopPath.moveTo(points.first().x, points.first().y)
            stopPath.addCircle(points.first().x, points.first().y, borderPathWidth + borderPathWidth, CW)

            canvas?.drawPath(stopPath, stopPaint)


            //TODO FInd a way to center this text in the circle
            canvas?.drawText("4", points.first().x, points.first().y, textPaint)

        } catch (e: Exception) {
            Log.e("TAG", e.message ?: "")
        }
    }

    fun addDataPoints(data: List<PointD>, point1: List<PointD>, point2: List<PointD>, @ColorRes drawColor: Int) {

        linePaint.apply {
            isAntiAlias = true
            strokeWidth = borderPathWidth
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = drawColor
        }

        stopPaint.apply {
            isAntiAlias = true
            strokeWidth = borderPathWidth
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = drawColor
        }

        post {
            Thread(Runnable {
                println(height)
                println(width)
                points.addAll(data.toF())
                conPoint1.addAll(point1.toF())
                conPoint2.addAll(point2.toF())

                postInvalidate()
                return@Runnable

            }).start()
        }
    }
}