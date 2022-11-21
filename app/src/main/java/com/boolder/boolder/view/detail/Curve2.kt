package com.boolder.boolder.view.detail

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * Created by Chan Myae Aung on 8/22/19.
 */
class RallyLineGraphChart : View {

    private val data = mutableListOf<DataPoint>()
    private val points = mutableListOf<PointF>()
    private val conPoint1 = mutableListOf<PointF>()
    private val conPoint2 = mutableListOf<PointF>()

    private val path = Path()
    private val borderPath = Path()
    private val borderPathPaint = Paint()

    private var viewCanvas: Canvas? = null
    private var bitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)

    private val borderPathWidth by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
        )
    }

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
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

            for (i in 1 until points.size) {
                path.cubicTo(
                    conPoint1[i - 1].x, conPoint1[i - 1].y, conPoint2[i - 1].x, conPoint2[i - 1].y,
                    points[i].x, points[i].y
                )
            }

            borderPath.set(path)

            canvas?.drawPath(borderPath, borderPathPaint)

        } catch (e: Exception) {
            error(e)
        }
    }

    private fun calculatePointsForData() {
        if (data.isEmpty()) return

        val bottomY = height - 30
        val xDiff =
            width.toFloat() / (data.size - 1) //subtract -1 because we want to include position at right side

        val maxData = data.maxBy { it.amount }!!.amount

        for (i in 0 until data.size) {
            val y = bottomY - (data[i].amount / maxData * (bottomY))
            println("COORDINATES $bottomY $xDiff $maxData $y ${xDiff * i}")
            points.add(PointF(xDiff * i, y))
            // 1. 0, 780
            // 2. 1080, 0
        }
    }

    private fun calculateConnectionPointsForBezierCurve() {
        try {
            for (i in 1 until points.size) {
                conPoint1.add(PointF((points[i].x + points[i - 1].x) / 2, points[i - 1].y))
                conPoint2.add(PointF((points[i].x + points[i - 1].x) / 2, points[i].y))
            }
        } catch (e: Exception) {
            error(e)
        }
    }

    private fun getLargeBarHeight() = height / 3 * 2f

    fun addDataPoints(data: List<PointF>, bzr1: List<PointF>, bzr2: List<PointF>) {


        post {
            Thread(Runnable {

                val pointMultiply = data.map {
                    val x = it.x
                    val y = it.y
                    PointF(x * 1000, y * 1000)
                }
                points.addAll(pointMultiply)

                val bzr1Multiply = bzr1.map {
                    val x = it.x
                    val y = it.y
                    PointF(x * 1000, y * 1000)
                }

                val bzr2Multiply = bzr2.map {
                    val x = it.x
                    val y = it.y
                    PointF(x * 1000, y * 1000)
                }

                conPoint1.addAll(bzr1Multiply)
                conPoint2.addAll(bzr2Multiply)


//                calculateConnectionPointsForBezierCurve()
                postInvalidate()
            }).start()
        }
//        post {
//            Thread(Runnable {
//
//                val oldPoints = points.toList()
//
//                if (oldPoints.isEmpty()) {
//                    this.data.addAll(data.toList())
//                    calculatePointsForData()
//                    calculateConnectionPointsForBezierCurve()
//                    postInvalidate()
//                    return@Runnable
//                }
//
//            }).start()
//        }
    }


}

data class DataPoint(val amount: Float)