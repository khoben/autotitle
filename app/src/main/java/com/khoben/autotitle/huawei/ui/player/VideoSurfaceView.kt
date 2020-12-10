package com.khoben.autotitle.huawei.ui.player

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import com.khoben.autotitle.huawei.App
import com.khoben.autotitle.huawei.service.mediaplayer.MediaSurfacePlayer
import com.khoben.autotitle.huawei.service.mediaplayer.VideoRender
import javax.inject.Inject
import kotlin.math.min


class VideoSurfaceView : GLSurfaceView {

    @Inject
    lateinit var render: VideoRender

    @Inject
    lateinit var mediaPlayer: MediaSurfacePlayer

    private var mVideoWidth = 0
    private var mVideoHeight = 0

    init {
        App.applicationComponent.inject(this)
        setEGLContextClientVersion(2)
        render.setMediaPlayer(mediaPlayer)
        setRenderer(render)
    }

    constructor(ctx: Context) : super(ctx, null)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        mVideoWidth = videoWidth
        mVideoHeight = videoHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize
                Log.d(TAG, "EXACTLY")
                // for compatibility, we adjust size based on aspect ratio
                when {
                    mVideoWidth * height < width * mVideoHeight -> {
                        Log.i(TAG, "Image too wide, correcting");
                        width = height * mVideoWidth / mVideoHeight
                    }
                    mVideoWidth * height > width * mVideoHeight -> {
                        Log.i(TAG, "Image too tall, correcting");
                        height = width * mVideoHeight / mVideoWidth
                    }
                    else -> {
                        // scale
                    }
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                Log.i(TAG, "W_EXACTLY")
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                Log.i(TAG, "H_EXACTLY")
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                Log.i(TAG, "AT_MOST $mVideoWidth x $mVideoHeight")
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth
                height = mVideoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                } else if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * mVideoHeight / mVideoWidth
                } else {
                    val scale = min(
                        widthSpecSize.toFloat() / mVideoWidth,
                        heightSpecSize.toFloat() / mVideoHeight
                    )
                    width = (mVideoWidth * scale).toInt()
                    height = (mVideoHeight * scale).toInt()
                    Log.i(TAG, "AT_MOST scale:$scale $width x $height")
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
            Log.i(TAG, "No size")
        }
        setMeasuredDimension(width, height)
    }


    override fun onResume() {
        queueEvent { render.setMediaPlayer(mediaPlayer) }
        super.onResume()
    }

    fun onDestroy() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

    companion object {
        private var TAG: String = VideoSurfaceView::class.java.simpleName
    }
}
