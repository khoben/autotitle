package com.khoben.autotitle.ui.overlay

import androidx.annotation.FloatRange
import kotlin.math.abs

enum class ShowAnchor(val flag: Int) {
    NONE(0),
    CENTER(1 shl 0),
    VERTICAL(1 shl 1),
    HORIZONTAL(1 shl 2),
}

open class Layout {

    private var surfaceWidth = 0
    private var surfaceHeight = 0

    /**
     * rotation relative to the layer center, in degrees
     */
    @FloatRange(from = 0.0, to = 360.0)
    private var rotationInDegrees = 0f

    private var scale = 0f

    /**
     * top left X coordinate, relative to parent canvas
     */
    private var x = 0f

    /**
     * top left Y coordinate, relative to parent canvas
     */
    private var y = 0f

    /**
     * is layer flipped horizontally (by X-coordinate)
     */
    private var isFlipped = false

    private lateinit var parent: OverlayEntity

    init {
        this.reset()
    }

    fun setSize(width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
    }

    protected open fun reset() {
        rotationInDegrees = 0.0f
        scale = 1.0f
        isFlipped = false
        x = 0.0f
        y = 0.0f
    }

    open fun getMaxScale(): Float {
        return MAX_SCALE
    }

    open fun getMinScale(): Float {
        return MIN_SCALE
    }

    private val EPS = 0.005F
    private val ROTATION_EPS = 250 * EPS
    private val TRANSLATION_EPS = 6 * EPS

    fun postRotate(rotationInDegreesDiff: Float): Int {
        var showAnchors = ShowAnchor.NONE.flag

        var newVal = (rotationInDegrees + rotationInDegreesDiff) % 360F

        if (newVal < 0) newVal += 360F

        if (abs(rotationInDegreesDiff) < ROTATION_EPS) {
            when {
                abs(newVal - 0F) < ROTATION_EPS -> {
                    newVal = 0F
                    showAnchors = ShowAnchor.CENTER.flag
                }
                abs(newVal - 360F) < ROTATION_EPS -> {
                    newVal = 360F
                    showAnchors = ShowAnchor.CENTER.flag
                }
                abs(newVal - 90F) < ROTATION_EPS -> {
                    newVal = 90F
                    showAnchors = ShowAnchor.CENTER.flag
                }
                abs(newVal - 180F) < ROTATION_EPS -> {
                    newVal = 180F
                    showAnchors = ShowAnchor.CENTER.flag
                }
                abs(newVal - 270F) < ROTATION_EPS -> {
                    newVal = 270F
                    showAnchors = ShowAnchor.CENTER.flag
                }
            }
        }

        rotationInDegrees = newVal
        return showAnchors
    }

    fun postTranslate(dx: Float, dy: Float): Int {
        var showAnchors = ShowAnchor.NONE.flag

        var newX = x + dx
        var newY = y + dy

        val center = parent.getCenterPoint()

        if (abs(dx) < TRANSLATION_EPS && abs(center.x + dx - 0.5F) < EPS) {
            newX = 0.5F - center.x + x
            showAnchors = showAnchors or ShowAnchor.HORIZONTAL.flag
        }

        if (abs(dy) < TRANSLATION_EPS && abs(center.y + dy - 0.5F) < EPS) {
            newY = 0.5F - center.y + y
            showAnchors = showAnchors or ShowAnchor.VERTICAL.flag
        }

        x = newX
        y = newY

        return showAnchors
    }

    fun postScale(scaleDiff: Float) {
        val newVal = scale * scaleDiff
        if (newVal >= getMinScale() && newVal <= getMaxScale()) {
            scale = newVal
        }
    }

    fun flip() {
        isFlipped = !isFlipped
    }

    open fun initialScale(): Float {
        return INITIAL_ENTITY_SCALE
    }

    fun getRotationInDegrees(): Float {
        return rotationInDegrees
    }

    fun setRotationInDegrees(@FloatRange(from = 0.0, to = 360.0) rotationInDegrees: Float) {
        this.rotationInDegrees = rotationInDegrees
    }

    fun getScale(): Float {
        return scale
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    fun getX(): Float {
        return x
    }

    fun setX(x: Float) {
        this.x = x
    }

    fun getY(): Float {
        return y
    }

    fun setY(y: Float) {
        this.y = y
    }

    fun isFlipped(): Boolean {
        return isFlipped
    }

    fun setFlipped(flipped: Boolean) {
        isFlipped = flipped
    }

    open fun initScale() {
        setScale(initialScale())
    }

    fun setParent(overlayEntity: OverlayEntity) {
        this.parent = overlayEntity
    }

    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 5.0f
        const val INITIAL_ENTITY_SCALE = 1.0f
    }
}