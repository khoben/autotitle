package com.khoben.autotitle.util

import android.graphics.PointF
import kotlin.math.sqrt

object MathUtils {

    fun getPointOnVector(x0: Float, y0: Float, x1: Float, y1: Float, rDiff: Float): PointF {
        val vectorLength = sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0))
        val k = rDiff / vectorLength
        return PointF(x0 + (x1 - x0) * k, y0 + (y1 - y0) * k)
    }

    /**
     * Normalizes degree value
     *
     * @param degrees Float
     * @return Float
     */
    fun adjustAngle(degrees: Float): Float {
        var deg = degrees
        if (deg > 180.0f) {
            deg -= 360.0f
        } else if (deg < -180.0f) {
            deg += 360.0f
        }
        return deg
    }

    /**
     * For more info:
     * [StackOverflow: How to check point is in rectangle](http://math.stackexchange.com/questions/190111/how-to-check-if-a-point-is-inside-a-rectangle)
     *
     * @param pt point to check
     * @param v1 vertex 1 of the triangle
     * @param v2 vertex 2 of the triangle
     * @param v3 vertex 3 of the triangle
     * @return true if point (x, y) is inside the triangle
     */
    fun pointInTriangle(
        pt: PointF, v1: PointF,
        v2: PointF, v3: PointF
    ): Boolean {
        val b1 = crossProduct(pt, v1, v2) < 0.0f
        val b2 = crossProduct(pt, v2, v3) < 0.0f
        val b3 = crossProduct(pt, v3, v1) < 0.0f
        return b1 == b2 && b2 == b3
    }

    /**
     * calculates cross product of vectors AB and AC
     *
     * @param a beginning of 2 vectors
     * @param b end of vector 1
     * @param c enf of vector 2
     * @return cross product AB * AC
     */
    private fun crossProduct(a: PointF, b: PointF, c: PointF): Float {
        return crossProduct(a.x, a.y, b.x, b.y, c.x, c.y)
    }

    /**
     * calculates cross product of vectors AB and AC
     *
     * @param ax X coordinate of point A
     * @param ay Y coordinate of point A
     * @param bx X coordinate of point B
     * @param by Y coordinate of point B
     * @param cx X coordinate of point C
     * @param cy Y coordinate of point C
     * @return cross product AB * AC
     */
    private fun crossProduct(
        ax: Float,
        ay: Float,
        bx: Float,
        by: Float,
        cx: Float,
        cy: Float
    ): Float {
        return (ax - cx) * (by - cy) - (bx - cx) * (ay - cy)
    }
}