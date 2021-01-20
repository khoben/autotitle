package com.khoben.autotitle.ui.overlay.gesture

import android.graphics.PointF
import kotlin.math.atan2
import kotlin.math.sqrt

internal class Vector2D @JvmOverloads constructor(x: Float = 0F, y: Float = 0F) : PointF(x, y) {

    private fun normalize() {
        val length = sqrt(x * x + y * y.toDouble()).toFloat()
        x /= length
        y /= length
    }

    companion object {
        fun getAngle(vector1: Vector2D, vector2: Vector2D): Float {
            vector1.normalize()
            vector2.normalize()
            val degrees = 180.0 / Math.PI * (atan2(
                vector2.y.toDouble(),
                vector2.x.toDouble()
            ) - atan2(vector1.y.toDouble(), vector1.x.toDouble()))
            return degrees.toFloat()
        }
    }
}