package com.boolder.boolder.utils

import android.graphics.Point
import android.util.Log

class CubicCurveAlgorithm {

    data class CubicCurveSegment(val controlPoint1: Point, val controlPoint2: Point)

    private val firstControlPoints = mutableListOf<Point>()
    private val secondControlPoints = mutableListOf<Point>()

    fun controlPointsFromPoints(dataPoints: List<Point>): List<CubicCurveSegment> {
        // Number of segments
        val count = dataPoints.size.dec()

        //P0, P1, P2, P3 are the points for each segment, where P0 & P3 are the knots and P1, P2 are the control points.
        if (count == 1) {
            val p0 = dataPoints[0]
            val p3 = dataPoints[1]


            //Calculate First Control Point
            //3P1 = 2P0 + P3
            val p1x = (2 * p0.x + p3.x) / 3
            val p1y = (2 * p0.y + p3.y) / 3

            firstControlPoints.add(Point(p1x, p1y))

            //Calculate second Control Point
            //P2 = 2P1 - P0
            val p2x = (2 * p1x + p0.x)
            val p2y = (2 * p1y + p0.y)

            secondControlPoints.add(Point(p2x, p2y))

        } else {

            val rhsArray = mutableListOf<Point>()
            //Array of Coefficients
            val a = mutableListOf<Int>()
            val b = mutableListOf<Int>()
            val c = mutableListOf<Int>()

            for (i in 0 until count) {
                var rhsValueX: Int
                var rhsValueY: Int

                val p0 = dataPoints[i]
                val p3 = dataPoints[i.inc()]

                when (i) {
                    0 -> {
                        a.add(0)
                        b.add(2)
                        c.add(1)

                        //rhs for first segment
                        rhsValueX = p0.x + 2 * p3.x
                        rhsValueY = p0.y + 2 * p3.y

                    }
                    count - 1 -> {
                        a.add(2)
                        b.add(7)
                        c.add(0)

                        //rhs for last segment
                        rhsValueX = 8 * p0.x + p3.x
                        rhsValueY = 8 * p0.y + p3.y
                    }
                    else -> {
                        a.add(1)
                        b.add(4)
                        c.add(1)

                        rhsValueX = 4 * p0.x + 2 * p3.x
                        rhsValueY = 4 * p0.y + 2 * p3.y
                    }
                }

                rhsArray.add(Point(rhsValueX, rhsValueY))
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

                rhsArray[i] = Point(r2x, r2y)
            }

            //Get First Control Points

            //Last control Point
            val lastControlPointX = rhsArray[count.dec()].x / b[count.dec()]
            val lastControlPointY = rhsArray[count.dec()].y / b[count.dec()]

            firstControlPoints[count.dec()] = Point(lastControlPointX, lastControlPointY)

            for (i in count - 2 downTo 0) {
                try {
                    val nextControlPoint = firstControlPoints[i.inc()]
                    val controlPointX = (rhsArray[i].x - c[i] * nextControlPoint.x) / b[i]
                    val controlPointY = (rhsArray[i].y - c[i] * nextControlPoint.y) / b[i]
                    firstControlPoints[i] = Point(controlPointX, controlPointY)
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
                        firstControlPoints[i] = Point(controlPointX, controlPointY)
                    }
                } else {
                    val p3 = dataPoints[i.inc()]
                    val nextP1 = firstControlPoints.getOrNull(i.inc())
                    if (nextP1 != null) {
                        val controlPointX = 2 * p3.x - nextP1.x
                        val controlPointY = 2 * p3.y - nextP1.y
                        firstControlPoints[i] = Point(controlPointX, controlPointY)
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