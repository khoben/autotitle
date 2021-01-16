package com.khoben.autotitle.ui.player

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.khoben.autotitle.service.mediaplayer.MediaSurfacePlayer
import com.khoben.autotitle.service.mediaplayer.VideoRender
import timber.log.Timber
import kotlin.math.min


class VideoSurfaceView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(ctx, attrs) {

    private var render: VideoRender? = null
    private var mediaPlayer: MediaSurfacePlayer? = null

    private var mVideoWidth = 0
    private var mVideoHeight = 0

    init {
        setEGLContextClientVersion(2)
    }

    fun init(render: VideoRender, mediaPlayer: MediaSurfacePlayer) {
        render.setMediaPlayer(mediaPlayer)
        setRenderer(render)
    }

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
                Timber.d("EXACTLY")
                // for compatibility, we adjust size based on aspect ratio
                when {
                    mVideoWidth * height < width * mVideoHeight -> {
                        Timber.d("Image too wide, correcting")
                        width = height * mVideoWidth / mVideoHeight
                    }
                    mVideoWidth * height > width * mVideoHeight -> {
                        Timber.d("Image too tall, correcting")
                        height = width * mVideoHeight / mVideoWidth
                    }
                    else -> {
                        // scale
                    }
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                Timber.d("W_EXACTLY")
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                Timber.d("H_EXACTLY")
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth
                height = mVideoHeight

                Timber.d("Video source: $mVideoWidth x $mVideoHeight")
                if (
                    (heightSpecMode == MeasureSpec.AT_MOST && widthSpecMode == MeasureSpec.AT_MOST) &&
                    ((height > heightSpecSize && width > widthSpecSize) ||
                            (height < heightSpecSize && width < widthSpecSize))
                ) {
                    val scale = min(
                        widthSpecSize.toFloat() / mVideoWidth,
                        heightSpecSize.toFloat() / mVideoHeight
                    )
                    width = (mVideoWidth * scale).toInt()
                    height = (mVideoHeight * scale).toInt()
                    Timber.d("AT_MOST ${width}x${height} (scale factor:$scale)")
                } else if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    val aspectRatio = mVideoWidth.toFloat() / mVideoHeight
                    height = heightSpecSize
                    width = (height * aspectRatio).toInt()
                    Timber.d("AT_MOST ${width}x${height}, fit height with source aspect ratio")
                } else if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    val aspectRatio = mVideoHeight.toFloat() / mVideoWidth
                    width = widthSpecSize
                    height = (width * aspectRatio).toInt()
                    Timber.d("AT_MOST ${width}x${height}, fit width with source aspect ratio")
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
            Timber.d("No size")
        }
        setMeasuredDimension(width, height)
    }


    override fun onResume() {
        if (render != null) queueEvent { render!!.setMediaPlayer(mediaPlayer) }
        super.onResume()
    }

    fun onDestroy() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying()) {
                mediaPlayer!!.stop()
            }
            mediaPlayer!!.release()
        }
    }
}
