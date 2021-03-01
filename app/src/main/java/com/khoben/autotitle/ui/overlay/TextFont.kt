package com.khoben.autotitle.ui.overlay

class TextFont {
    /**
     * color value (ex: 0xFF00FF)
     */
    private var color = 0

    /**
     * name of the font
     */
    private var typeface: String? = null

    /**
     * size of the font, relative to parent
     */
    private var size = 0f

    fun increaseSize(diff: Float) {
        size += diff
    }

    fun decreaseSize(diff: Float) {
        if (size - diff >= MIN_FONT_SIZE) {
            size -= diff
        }
    }

    fun getColor(): Int {
        return color
    }

    fun setColor(color: Int) {
        this.color = color
    }

    fun getTypeface(): String? {
        return typeface
    }

    fun setTypeface(typeface: String?) {
        this.typeface = typeface
    }

    fun getSize(): Float {
        return size
    }

    fun setSize(size: Float) {
        this.size = size
    }

    companion object {
        const val MIN_FONT_SIZE = 0.01f
    }
}