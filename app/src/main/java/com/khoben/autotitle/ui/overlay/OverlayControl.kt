package com.khoben.autotitle.ui.overlay

import android.graphics.Bitmap
import android.graphics.RectF

enum class Position {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

data class OverlayControl(
    /**
     * Control's icon
     */
    val bitmap: Bitmap,
    /**
     * Control's [bitmap] width
     */
    val width: Float,
    /**
     * Control's [bitmap] height
     */
    val height: Float,
    /**
     * Current control's bounding rect
     */
    val rect: RectF,
    /**
     * One of four possible positions:
     *
     * [Position.TOP_LEFT]
     *
     * [Position.TOP_RIGHT]
     *
     * [Position.BOTTOM_LEFT]
     *
     * [Position.BOTTOM_RIGHT]
     */
    val position: Position
) {
    /**
     *  Get bounding rect for [OverlayControl] object depending
     *  on provided coordinates (clockwise order) and [position]
     *
     * @param x0 Float
     * @param y0 Float
     * @param x1 Float
     * @param y1 Float
     * @param x2 Float
     * @param y2 Float
     * @param x3 Float
     * @param y3 Float
     * @return RectF
     */
    fun getPosition(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float
    ): RectF {
        return when (position) {
            Position.TOP_LEFT -> {
                RectF(
                    x0 - width / 2,
                    y0 - height / 2,
                    x0 + width / 2,
                    y0 + height / 2
                )
            }
            Position.TOP_RIGHT -> {
                RectF(
                    x1 - width / 2,
                    y1 - height / 2,
                    x1 + width / 2,
                    y1 + height / 2
                )
            }
            Position.BOTTOM_LEFT -> {
                RectF(
                    x3 - width / 2,
                    y3 - height / 2,
                    x3 + width / 2,
                    y3 + height / 2
                )
            }
            Position.BOTTOM_RIGHT -> {
                RectF(
                    x2 - width / 2,
                    y2 - height / 2,
                    x2 + width / 2,
                    y2 + height / 2
                )
            }
        }.also {
            rect.set(it)
        }
    }
}
