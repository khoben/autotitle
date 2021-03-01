package com.khoben.autotitle.ui.overlay

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import androidx.annotation.IntRange
import com.khoben.autotitle.util.MathUtils

abstract class OverlayEntity(
    protected val layout: Layout,
    @IntRange(from = 1) protected var canvasWidth: Int,
    @IntRange(from = 1) protected var canvasHeight: Int
) {

    protected val matrix = Matrix()

    /**
     * x0, y0, x1, y1, x2, y2, x3, y3, x0, y0 : clockwise
     */
    private val destPoints = FloatArray(10)

    /**
     * x0, y0, x1, y1, x2, y2, x3, y3, x0, y0 : clockwise
     */
    protected val srcPoints = FloatArray(10)

    private var controls: List<OverlayControl>? = null

    /**
     * maximum scale of the initial image, so that
     * the entity still fits within the parent canvas
     */
    protected var holyScale = 0f

    protected var startTimeMs: Long = 0L
    protected var endTimeMs: Long = 0L
    protected var isVisible = true

    init {
        layout.setSize(canvasWidth, canvasHeight)
        layout.setParent(this)
    }

    /**
     * S - scale matrix, R - rotate matrix, T - translate matrix,
     * L - result transformation matrix
     *
     *
     * The correct order of applying transformations is : L = S * R * T
     *
     *
     * See more info: [Game Dev: Transform Matrix multiplication order](http://gamedev.stackexchange.com/questions/29260/transform-matrix-multiplication-order)
     *
     *
     * Preconcat works like M` = M * S, so we apply preScale -> preRotate -> preTranslate
     * the result will be the same: L = S * R * T
     *
     *
     * NOTE: postconcat (postScale, etc.) works the other way : M` = S * M, in order to use it
     * we'd need to reverse the order of applying
     * transformations : post holy scale ->  postTranslate -> postRotate -> postScale
     */
    protected open fun updateMatrix() {
        // init matrix to E - identity matrix
        matrix.reset()
        val topLeftX = layout.getX() * canvasWidth
        val topLeftY = layout.getY() * canvasHeight
        val centerX = topLeftX + getWidth() * holyScale * 0.5f
        val centerY = topLeftY + getHeight() * holyScale * 0.5f

        // calculate params
        var rotationInDegree = layout.getRotationInDegrees()
        var scaleX = layout.getScale()
        val scaleY = layout.getScale()
        if (layout.isFlipped()) {
            // flip (by X-coordinate) if needed
            rotationInDegree *= -1.0f
            scaleX *= -1.0f
        }

        // applying transformations : L = S * R * T

        // scale
        matrix.preScale(scaleX, scaleY, centerX, centerY)

        // rotate
        matrix.preRotate(rotationInDegree, centerX, centerY)

        // translate
        matrix.preTranslate(topLeftX, topLeftY)

        // applying holy scale - S`, the result will be : L = S * R * T * S`
        matrix.preScale(holyScale, holyScale)
    }

    @JvmName("setControls1")
    fun setControls(controls: List<OverlayControl>) {
        this.controls = controls
    }

    fun absoluteX(): Float {
        return layout.getX() * canvasWidth
    }

    fun absoluteY(): Float {
        return layout.getY() * canvasHeight
    }

    fun absoluteCenterX(): Float {
        val topLeftX = layout.getX() * canvasWidth
        return topLeftX + getWidth() * holyScale * 0.5f
    }

    fun absoluteCenterY(): Float {
        val topLeftY = layout.getY() * canvasHeight
        return topLeftY + getHeight() * holyScale * 0.5f
    }

    fun absoluteCenter(): PointF {
        return PointF(absoluteCenterX(), absoluteCenterY())
    }

    private val centerPointSrc = floatArrayOf(this.getWidth() / 2F, this.getHeight() / 2F)
    private val centerPointDst = floatArrayOf(0F, 0F)


    fun getCenterPoint(): PointF {
        updateMatrix()
        matrix.mapPoints(centerPointDst, centerPointSrc)
        val x = (destPoints[0] + destPoints[2] + destPoints[4] + destPoints[6]) / 4F
        val y = (destPoints[1] + destPoints[3] + destPoints[5] + destPoints[7]) / 4F
        return PointF(x / canvasWidth, y / canvasHeight)
    }

    fun moveToCanvasCenter() {
        moveCenterTo(PointF(canvasWidth * 0.5f, canvasHeight * 0.5f))
    }

    fun moveCenterTo(moveToCenter: PointF) {
        val currentCenter = absoluteCenter()
        layout.postTranslate(
            1.0f * (moveToCenter.x - currentCenter.x) / canvasWidth,
            1.0f * (moveToCenter.y - currentCenter.y) / canvasHeight
        )
    }

    private val pA = PointF()
    private val pB = PointF()
    private val pC = PointF()
    private val pD = PointF()

    /**
     * For more info:
     * [StackOverflow: How to check point is in rectangle](http://math.stackexchange.com/questions/190111/how-to-check-if-a-point-is-inside-a-rectangle)
     *
     * NOTE: it's easier to apply the same transformation matrix (calculated before) to the original source points, rather than
     * calculate the result points ourselves
     * @param point point
     * @return true if point (x, y) is inside the triangle
     */
    fun pointInLayerRect(point: PointF): Boolean {
        updateMatrix()
        // map rect vertices
        matrix.mapPoints(destPoints, srcPoints)
        pA.x = destPoints[0]
        pA.y = destPoints[1]
        pB.x = destPoints[2]
        pB.y = destPoints[3]
        pC.x = destPoints[4]
        pC.y = destPoints[5]
        pD.x = destPoints[6]
        pD.y = destPoints[7]
        return MathUtils.pointInTriangle(point, pA, pB, pC) ||
                MathUtils.pointInTriangle(point, pA, pD, pC)
    }

    fun draw(canvas: Canvas, paint: Paint? = null) {
        updateMatrix()
        drawContent(canvas, paint)
    }

    fun drawSelected(canvas: Canvas, framePaint: Paint) {
        drawBorderFrame(canvas, framePaint)
    }

    private fun drawBorderFrame(canvas: Canvas, framePaint: Paint) {
        updateMatrix()
        matrix.mapPoints(destPoints, srcPoints)

        canvas.drawLines(destPoints, 0, 8, framePaint)
        canvas.drawLines(destPoints, 2, 8, framePaint)

        controls?.forEach { control ->
            canvas.drawBitmap(
                control.bitmap,
                null,
                control.getPosition(
                    destPoints[0],
                    destPoints[1],
                    destPoints[2],
                    destPoints[3],
                    destPoints[4],
                    destPoints[5],
                    destPoints[6],
                    destPoints[7]
                ),
                null
            )
        }
    }

    fun drawCenterLines(canvas: Canvas, paint: Paint) {
        updateMatrix()
        matrix.mapPoints(destPoints, srcPoints)

//        val centerPoint = floatArrayOf(absoluteCenterX(), absoluteCenterY())
//        matrix.mapPoints(centerPoint)

        var x0 = (destPoints[0] + destPoints[6]) / 2F
        var y0 = (destPoints[1] + destPoints[7]) / 2F
        var x1 = (destPoints[2] + destPoints[4]) / 2F
        var y1 = (destPoints[3] + destPoints[5]) / 2F

        val vStart = MathUtils.getPointOnVector(x0, y0, x1, y1, layout.getScale() * getWidth() / 2F - 50)
        val vEnd = MathUtils.getPointOnVector(x0, y0, x1, y1, layout.getScale() * getWidth() / 2F + 50)

        canvas.drawLine(vStart.x, vStart.y, vEnd.x, vEnd.y, paint)

        x0 = (destPoints[0] + destPoints[2]) / 2F
        y0 = (destPoints[1] + destPoints[3]) / 2F
        x1 = (destPoints[4] + destPoints[6]) / 2F
        y1 = (destPoints[5] + destPoints[7]) / 2F

        val hStart = MathUtils.getPointOnVector(x0, y0, x1, y1, layout.getScale() * getHeight() / 2F - 50)
        val hEnd = MathUtils.getPointOnVector(x0, y0, x1, y1, layout.getScale() * getHeight() / 2F + 50)

        canvas.drawLine(hStart.x, hStart.y, hEnd.x, hEnd.y, paint)
    }

    fun postTranslate(dx: Float, dy: Float): Int {
        return layout.postTranslate(dx, dy)
    }

    fun postRotate(rotation: Float): Int {
        return layout.postRotate(rotation)
    }

    fun postScale(scale: Float) {
        layout.postScale(scale)
    }

    fun postConcat(matrix: Matrix) {
        this.matrix.postConcat(matrix)
    }

    fun initScale() {
        layout.initScale()
    }

    abstract fun drawContent(canvas: Canvas, paint: Paint?)

    abstract fun getWidth(): Int

    abstract fun getHeight(): Int

    open fun release() {}

    @Throws(Throwable::class)
    protected open fun finalize() {
        try {
            release()
        } finally {
        }
    }
}
