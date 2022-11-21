package com.boolder.boolder.utils

import android.graphics.PointF
import android.util.Log

class CubicCurveAlgorithm {

    data class CubicCurveSegment(val controlPoint1: PointF, val controlPoint2: PointF)

    private var firstControlPoints = mutableListOf<PointF>()
    private val secondControlPoints = mutableListOf<PointF>()

    fun controlPointsFromPoints(dataPoints: List<PointF>): List<CubicCurveSegment> {
        // Number of segments
        val count = dataPoints.size.dec()

        //P0, P1, P2, P3 are the points for each segment, where P0 & P3 are the knots and P1, P2 are the control points.
        if (count == 1) {
            val p0 = dataPoints[0]
            val p3 = dataPoints[1]


            //Calculate First Control PointF
            //3P1 = 2P0 + P3
            val p1x = (2 * p0.x + p3.x) / 3
            val p1y = (2 * p0.y + p3.y) / 3

            firstControlPoints.add(PointF(p1x, p1y))

            //Calculate second Control PointF
            //P2 = 2P1 - P0
            val p2x = (2 * p1x + p0.x)
            val p2y = (2 * p1y + p0.y)

            secondControlPoints.add(PointF(p2x, p2y))

        } else {

            firstControlPoints = MutableList(count) { PointF(0f, 0f) }

            val rhsArray = mutableListOf<PointF>()
            //Array of Coefficients
            val a = mutableListOf<Float>()
            val b = mutableListOf<Float>()
            val c = mutableListOf<Float>()

            for (i in 0 until count) {
                var rhsValueX: Float
                var rhsValueY: Float

                val p0 = dataPoints[i]
                val p3 = dataPoints[i.inc()]

                when (i) {
                    0 -> {
                        a.add(0f)
                        b.add(2f)
                        c.add(1f)

                        //rhs for first segment
                        rhsValueX = p0.x + 2 * p3.x
                        rhsValueY = p0.y + 2 * p3.y

                    }
                    count - 1 -> {
                        a.add(2f)
                        b.add(7f)
                        c.add(0f)

                        //rhs for last segment
                        rhsValueX = 8 * p0.x + p3.x
                        rhsValueY = 8 * p0.y + p3.y
                    }
                    else -> {
                        a.add(1f)
                        b.add(4f)
                        c.add(1f)

                        rhsValueX = 4 * p0.x + 2 * p3.x
                        rhsValueY = 4 * p0.y + 2 * p3.y
                    }
                }

                rhsArray.add(PointF(rhsValueX, rhsValueY))
            }

            //Solve Ax=B. Use Tridiagonal matrix algorithm a.k.a Thomas Algorithm
            for (i in 1 until count) {
                val rhsValueX = rhsArray[i].x
                val rhsValueY = rhsArray[i].y

                val prevRhsValueX = rhsArray[i.dec()].x
                val prevRhsValueY = rhsArray[i.dec()].y

                val m = a[i] / b[i.dec()]
                val b1 = b[i] - m * c[i.dec()]
                b[i] = b1

                val r2x = rhsValueX - m * prevRhsValueX
                val r2y = rhsValueY - m * prevRhsValueY

                rhsArray[i] = PointF(r2x, r2y)
            }

            //Get First Control Points

            //Last control PointF
            val lastControlPointX = rhsArray[count.dec()].x / b[count.dec()]
            val lastControlPointY = rhsArray[count.dec()].y / b[count.dec()]

            firstControlPoints[count.dec()] = PointF(lastControlPointX, lastControlPointY)

            for (i in count - 2 downTo 0) {
                try {
                    val nextControlPoint = firstControlPoints[i.inc()]
                    val controlPointX = (rhsArray[i].x - c[i] * nextControlPoint.x) / b[i]
                    val controlPointY = (rhsArray[i].y - c[i] * nextControlPoint.y) / b[i]
                    firstControlPoints[i] = PointF(controlPointX, controlPointY)
                } catch (e: Exception) {
                    Log.w("Cubic Curve Algorithm", e.message ?: "No message")
                }
            }

            //Compute second Control Points from first
            for (i in 0 until count) {
                if (i == count.dec()) {
                    val p3 = dataPoints[i.inc()]
                    val p1 = firstControlPoints.getOrNull(i)
                    if (p1 != null) {
                        val controlPointX = (p3.x + p1.x) / 2
                        val controlPointY = (p3.y + p1.y) / 2
                        secondControlPoints.add(PointF(controlPointX, controlPointY))
                    }
                } else {
                    val p3 = dataPoints[i.inc()]
                    val nextP1 = firstControlPoints.getOrNull(i.inc())
                    if (nextP1 != null) {
                        val controlPointX = 2 * p3.x - nextP1.x
                        val controlPointY = 2 * p3.y - nextP1.y
                        secondControlPoints.add(PointF(controlPointX, controlPointY))
                    }
                }
            }
        }

        val controlPoints = mutableListOf<CubicCurveSegment>()
        for (i in 0 until count) {
            val firstControlPoint = firstControlPoints.getOrNull(i)
            if (firstControlPoint != null) {
                val secondControlPoint = secondControlPoints[i]
                val segment = CubicCurveSegment(firstControlPoint, secondControlPoint)
                controlPoints.add(segment)
            }
        }

        return controlPoints
    }
}