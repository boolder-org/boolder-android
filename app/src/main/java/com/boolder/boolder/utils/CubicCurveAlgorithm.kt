package com.boolder.boolder.utils

import android.util.Log
import com.boolder.boolder.view.detail.PointD

class CubicCurveAlgorithm {

    data class CubicCurveSegment(val controlPoint1: PointD, val controlPoint2: PointD)

    private var firstControlPoints = mutableListOf<PointD>()
    private val secondControlPoints = mutableListOf<PointD>()

    fun controlPointsFromPoints(dataPoints: List<PointD>): List<CubicCurveSegment> {
        // Number of segments
        val count = dataPoints.size.dec()

        //P0, P1, P2, P3 are the points for each segment, where P0 & P3 are the knots and P1, P2 are the control points.
        if (count == 1) {
            val p0 = dataPoints[0]
            val p3 = dataPoints[1]


            //Calculate First Control com.boolder.boolder.view.detail.PointD)
            //3P1 = 2P0 + P3
            val p1x = (2 * p0.x + p3.x) / 3
            val p1y = (2 * p0.y + p3.y) / 3

            firstControlPoints.add(PointD(p1x, p1y))

            //Calculate second Control com.boolder.boolder.view.detail.PointD)
            //P2 = 2P1 - P0
            val p2x = (2 * p1x + p0.x)
            val p2y = (2 * p1y + p0.y)

            secondControlPoints.add(PointD(p2x, p2y))

        } else {

            firstControlPoints = MutableList(count) { PointD(0.0, 0.0) }

            val rhsArray = mutableListOf<PointD>()
            //Array of Coefficients
            val a = mutableListOf<Double>()
            val b = mutableListOf<Double>()
            val c = mutableListOf<Double>()

            for (i in 0 until count) {
                var rhsValueX: Double
                var rhsValueY: Double

                val p0 = dataPoints[i]
                val p3 = dataPoints[i.inc()]

                when (i) {
                    0 -> {
                        a.add(0.0)
                        b.add(2.0)
                        c.add(1.0)

                        //rhs for first segment
                        rhsValueX = p0.x + 2 * p3.x
                        rhsValueY = p0.y + 2 * p3.y

                    }
                    count - 1 -> {
                        a.add(2.0)
                        b.add(7.0)
                        c.add(0.0)

                        //rhs for last segment
                        rhsValueX = 8 * p0.x + p3.x
                        rhsValueY = 8 * p0.y + p3.y
                    }
                    else -> {
                        a.add(1.0)
                        b.add(4.0)
                        c.add(1.0)

                        rhsValueX = 4 * p0.x + 2 * p3.x
                        rhsValueY = 4 * p0.y + 2 * p3.y
                    }
                }

                rhsArray.add(PointD(rhsValueX, rhsValueY))
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

                rhsArray[i] = PointD(r2x, r2y)
            }

            //Get First Control Points

            //Last control com.boolder.boolder.view.detail.PointD)
            val lastControlPointX = rhsArray[count.dec()].x / b[count.dec()]
            val lastControlPointY = rhsArray[count.dec()].y / b[count.dec()]
            firstControlPoints[count.dec()] = PointD(lastControlPointX, lastControlPointY)

            for (i in count - 2 downTo 0) {
                try {
                    val nextControlPoint = firstControlPoints[i.inc()]
                    val controlPointX = (rhsArray[i].x - c[i] * nextControlPoint.x) / b[i]
                    val controlPointY = (rhsArray[i].y - c[i] * nextControlPoint.y) / b[i]
                    firstControlPoints[i] = PointD(controlPointX, controlPointY)
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
                        secondControlPoints.add(PointD(controlPointX, controlPointY))
                    }
                } else {
                    val p3 = dataPoints[i.inc()]
                    val nextP1 = firstControlPoints.getOrNull(i.inc())
                    if (nextP1 != null) {
                        val controlPointX = 2 * p3.x - nextP1.x
                        val controlPointY = 2 * p3.y - nextP1.y
                        secondControlPoints.add(PointD(controlPointX, controlPointY))
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