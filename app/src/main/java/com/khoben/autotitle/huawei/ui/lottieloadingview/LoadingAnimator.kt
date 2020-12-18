package com.khoben.autotitle.huawei.ui.lottieloadingview

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RelativeLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable


class LoadingAnimator constructor(private val context: Activity, animationName: String) {
    private var lottieAnimationView: LottieAnimationView = LottieAnimationView(context)

    private var rLayout: RelativeLayout = RelativeLayout(context)
    private var rLayoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.FILL_PARENT,
        RelativeLayout.LayoutParams.FILL_PARENT
    )

    private val lLayoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT
    )

    init {
        lottieAnimationView.setAnimation(animationName)
        lottieAnimationView.layoutParams = lLayoutParams
        rLayout.addView(lottieAnimationView)
    }

    fun play() {
        lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        context.setContentView(rLayout, rLayoutParams)
        lottieAnimationView.playAnimation()
    }

    fun stop(originalView: View) {
        Handler(Looper.getMainLooper()).postDelayed({
            lottieAnimationView.cancelAnimation()
            context.setContentView(originalView)
        }, 500)
    }
}