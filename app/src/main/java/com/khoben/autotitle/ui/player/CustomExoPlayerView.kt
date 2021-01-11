package com.khoben.autotitle.ui.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ui.PlayerView
import com.khoben.autotitle.R

class CustomExoPlayerView(context: Context, attrs: AttributeSet) : PlayerView(context, attrs) {
    private val playButton: View = findViewById(R.id.exo_play)
    private val fakePlayButton: View = findViewById(R.id.exo_play_fake)
    private val progressBar: View = findViewById(R.id.exo_seekbar)
    private var firstTimeHide = true

    init {

        fakePlayButton.setOnClickListener {
            fakePlayButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            playButton.performClick()

        }

        setControllerVisibilityListener { visible ->
            if (visible == View.VISIBLE && firstTimeHide) {
                progressBar.isVisible = false
                firstTimeHide = false
            }
        }
    }

}