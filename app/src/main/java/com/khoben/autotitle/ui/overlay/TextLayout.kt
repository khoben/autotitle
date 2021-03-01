package com.khoben.autotitle.ui.overlay

class TextLayout : Layout() {
    private var text: CharSequence? = null
    private var font: TextFont? = null

    fun setFont(font: TextFont) {
        this.font = font
    }

    fun getFont(): TextFont? {
        return font
    }

    fun getText(): CharSequence? {
        return text
    }

    fun setText(text: String?) {
        this.text = text
    }

    override fun reset() {
        super.reset()
        text = ""
        font = TextFont()
    }

    override fun getMaxScale(): Float {
        return MAX_SCALE
    }

    override fun getMinScale(): Float {
        return MIN_SCALE
    }

    override fun initialScale(): Float {
        return INITIAL_SCALE
    }

    override fun initScale() {
        setScale(initialScale())
    }

    companion object {
        /**
         * limit text size to view bounds
         * so that users don't put small font size and scale it 100+ times
         */
        const val MAX_SCALE = 1.0f
        const val MIN_SCALE = 0.25f
        const val MIN_BITMAP_HEIGHT = 0.13f
        const val FONT_SIZE_STEP = 0.008f
        const val INITIAL_FONT_SIZE = 0.15f
        const val INITIAL_FONT_COLOR = -0x1000000
        const val INITIAL_SCALE = 0.5f // set the same to avoid text scaling
    }
}