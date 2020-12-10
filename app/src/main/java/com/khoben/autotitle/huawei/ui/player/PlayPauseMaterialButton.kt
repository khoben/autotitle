package com.khoben.autotitle.huawei.ui.player

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.khoben.autotitle.huawei.R

class PlayPauseMaterialButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val playIcon = ContextCompat.getDrawable(context, R.drawable.play_to_pause_btn)!!
    private val pauseIcon = ContextCompat.getDrawable(context, R.drawable.pause_to_play_btn)!!

    private val animatedDrawableButton: ImageView

    private var avd: AnimatedVectorDrawableCompat? = null
    private var avd2: AnimatedVectorDrawable? = null

    private var lastPlayPauseButtonState = false

    init {
        LayoutInflater.from(context).inflate(R.layout.play_pause_button_layout, this)
        animatedDrawableButton = findViewById(R.id.bigicon_play)
        isClickable = true
    }

    fun getState() = lastPlayPauseButtonState

    fun toggle() {
        toggle(!lastPlayPauseButtonState)
    }

    fun toggle(state: Boolean) {
        if (lastPlayPauseButtonState == state) return
        lastPlayPauseButtonState = state
        if (state) {
            animatedDrawableButton.setImageDrawable(playIcon)
            val drawable = animatedDrawableButton.drawable
            if (drawable is AnimatedVectorDrawableCompat) {
                avd = drawable
                avd!!.start()
            } else if (drawable is AnimatedVectorDrawable) {
                avd2 = drawable
                avd2!!.start()
            }
        } else {
            animatedDrawableButton.setImageDrawable(pauseIcon)
            val drawable = animatedDrawableButton.drawable
            if (drawable is AnimatedVectorDrawableCompat) {
                avd = drawable
                avd!!.start()
            } else if (drawable is AnimatedVectorDrawable) {
                avd2 = drawable
                avd2!!.start()
            }
        }
    }
}