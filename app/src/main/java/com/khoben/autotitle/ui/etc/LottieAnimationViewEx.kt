package com.khoben.autotitle.ui.etc

import android.content.Context
import android.util.AttributeSet
import com.airbnb.lottie.LottieAnimationView
import com.khoben.autotitle.R

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
                if (xmlMaxFrame > 0) {
                    setMaxFrame(xmlMaxFrame)
                }
                if (xmlMinFrame > 0) {
                    setMinFrame(xmlMinFrame)
                }
            } finally {
                recycle()
            }
        }
    }
}