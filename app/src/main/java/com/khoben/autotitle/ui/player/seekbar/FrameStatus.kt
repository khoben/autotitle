package com.khoben.autotitle.ui.player.seekbar

/**
 * Async frames loading statuses
 */
enum class FrameStatus {
    /**
     * Emits all empty frames
     */
    PRELOAD,

    /**
     * Emits single processed frame
     */
    LOAD_SINGLE,

    /**
     * Emits all processed frames
     */
    COMPLETED
}
