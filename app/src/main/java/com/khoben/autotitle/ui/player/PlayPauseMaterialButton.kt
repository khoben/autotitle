package com.khoben.autotitle.ui.player

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.khoben.autotitle.R

class PlayPauseMaterialButton(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    private val playPauseIcon = ContextCompat.getDrawable(context, R.drawable.play_to_pause_btn)!!
    private val pausePlayIcon = ContextCompat.getDrawable(context, R.drawable.pause_to_play_btn)!!

    private val animatedDrawableButton: ImageView

    private var animatedDrawableCompat: AnimatedVectorDrawableCompat? = null
    private var animatedDrawable: AnimatedVectorDrawable? = null

    /**
     * False - Play icon
     *
     * True - Pause icon
     */
    private var currentState = false

    init {
        LayoutInflater.from(context).inflate(R.layout.play_pause_button_layout, this)
        animatedDrawableButton = findViewById(R.id.bigicon_play)
        isClickable = true
    }

    fun getState() = currentState

    fun toggle() = toggle(!currentState)

    fun toggle(state: Boolean) {
        if (currentState == state) return
        currentState = state
        if (state) {
            animatedDrawableButton.setImageDrawable(playPauseIcon)
        } else {
            animatedDrawableButton.setImageDrawable(pausePlayIcon)
        }
        val drawable = animatedDrawableButton.drawable
        if (drawable is AnimatedVectorDrawableCompat) {
            animatedDrawableCompat = drawable
            animatedDrawableCompat!!.start()
        } else if (drawable is AnimatedVectorDrawable) {
            animatedDrawable = drawable
            animatedDrawable!!.start()
        }
    }
}