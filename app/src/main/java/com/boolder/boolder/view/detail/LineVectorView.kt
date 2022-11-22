package com.boolder.boolder.view.detail

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes


class LineVectorView @JvmOverloads constructor(
    context: Context?,
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

    private val paint = Paint()

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
            if (points.isEmpty() && controlPoint1.isEmpty() && controlPoint2.isEmpty()) return

            path.reset()
            path.moveTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                path.cubicTo(
                    controlPoint1[i.dec()].x,
                    controlPoint1[i.dec()].y,
                    controlPoint2[i.dec()].x,
                    controlPoint2[i.dec()].y,
                    points[i].x,
                    points[i].y
                )
            }

            canvas?.drawPath(path, paint)

        } catch (e: Exception) {
            Log.e("TAG", e.message ?: "")
        }
    }

    fun addDataPoints(data: List<PointD>, point1: List<PointD>, point2: List<PointD>, @ColorRes drawColor: Int) {

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

                points.addAll(data.toF())
                controlPoint1.addAll(point1.toF())
                controlPoint2.addAll(point2.toF())

                postInvalidate()

            }.start()
        }
    }
}