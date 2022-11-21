package com.boolder.boolder.view.detail

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.boolder.boolder.utils.CubicCurveAlgorithm.CubicCurveSegment

class CurveGraphChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val path = Path()
    private val borderPath = Path()

    //    private val barPaint = Paint()
//    private val pathPaint = Paint()
    private val borderPathPaint = Paint()

    // Data from CubicCurveAlgorithm
    private lateinit var points: List<PointF>
    private lateinit var segment: List<CubicCurveSegment>

    private var viewCanvas: Canvas? = null
    private var bitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)

    private val barWidth by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, .5f, resources.displayMetrics
        )
    }

    private val borderPathWidth by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
        )
    }

    fun setup(points: List<PointF>, segment: List<CubicCurveSegment>) {
        this.points = points
        this.segment = segment
        postInvalidate()
    }

    init {
        init(attrs)
    }

    private fun init(set: AttributeSet?) {

        borderPathPaint.apply {
            isAntiAlias = true
            strokeWidth = borderPathWidth
            style = Paint.Style.STROKE
            color = Color.GREEN
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val p1 = PointF(1f, 1f)
        val p2 = PointF(0f, 0f)

        path.reset()
        path.moveTo(p1.x, p1.y)
        path.lineTo(p2.x, p2.y)
        borderPath.set(path)
        canvas?.drawPath(borderPath, borderPathPaint)

//        if (this::points.isInitialized && points.isNotEmpty()) {
//            drawBezierCurve(canvas)
//            bitmap?.let {
//                canvas?.drawBitmap(it, 0f, 0f, bitmapPaint)
//            }
//        }
    }

    private fun drawBezierCurve(canvas: Canvas?) {

        try {

            path.reset()
            path.moveTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                path.cubicTo(
                    segment[i.dec()].controlPoint1.x,
                    segment[i.dec()].controlPoint1.y,
                    segment[i.dec()].controlPoint2.x,
                    segment[i.dec()].controlPoint2.y,
                    points[i].x,
                    points[i].y
                )
            }

            borderPath.set(path)

            path.lineTo(width.toFloat(), height.toFloat())
            path.lineTo(0f, height.toFloat())

//            canvas?.drawPath(path, pathPaint)

            canvas?.drawPath(borderPath, borderPathPaint)

        } catch (e: Exception) {
            Log.w("TAG", e.message ?: "")
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
//        drawVerticalBars(viewCanvas)
    }

}