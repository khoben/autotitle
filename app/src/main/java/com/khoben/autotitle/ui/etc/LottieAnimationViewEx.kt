package com.khoben.autotitle.ui.etc

import android.content.Context
import android.util.AttributeSet
import com.airbnb.lottie.LottieAnimationView
import com.khoben.autotitle.R
import timber.log.Timber

class LottieAnimationViewEx(context: Context, attrs: AttributeSet?) :
        LottieAnimationView(context, attrs) {
    init {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.LottieAnimationViewEx,
                0, 0).apply {
            try {
                val xmlMaxFrame = getInteger(R.styleable.LottieAnimationViewEx_lottie_maxFrame, 0)
                val xmlMinFrame = getInteger(R.styleable.LottieAnimationViewEx_lottie_minFrame, 0)
                val xmlRotation = getFloat(R.styleable.LottieAnimationViewEx_lottie_rotation, 0F)
                if (xmlMaxFrame > 0) {
                    setMaxFrame(xmlMaxFrame)
                }
                if (xmlMinFrame > 0) {
                    setMinFrame(xmlMinFrame)
                }
                if (xmlRotation != 0F) {
                    rotation = xmlRotation
                }
            } finally {
                recycle()
            }
        }
    }
}