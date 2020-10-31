package com.khoben.autotitle.huawei.common.eventbus

import com.khoben.autotitle.huawei.model.PlaybackPlayer
import com.khoben.autotitle.huawei.model.PlaybackUI
import com.khoben.autotitle.huawei.model.PlaybackUIState
import org.greenrobot.eventbus.EventBus

object EventController {
    object Player {
        fun sendCompletion() {
            EventBus.getDefault().post(PlaybackPlayer.COMPLETION)
        }

        fun sendPrepared() {
            EventBus.getDefault().post(PlaybackPlayer.PREPARED)
        }
    }

    object UI {
        fun sendPause() {
            EventBus.getDefault().post(PlaybackUI(PlaybackUIState.PAUSED))
        }

        fun sendPlay() {
            EventBus.getDefault().post(PlaybackUI(PlaybackUIState.PLAY))
        }

        fun sendToggle() {
            EventBus.getDefault().post(PlaybackUI(PlaybackUIState.TOGGLE))
        }

        fun sendRewind(time: Long) {
            EventBus.getDefault().post(PlaybackUI(PlaybackUIState.REWIND, time))
        }
    }
}