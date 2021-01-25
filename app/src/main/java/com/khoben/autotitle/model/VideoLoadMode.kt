package com.khoben.autotitle.model

enum class VideoLoadMode {
    /**
     * Video loading with enabled caption auto-detection
     */
    AUTO_DETECT,

    /**
     * Video loading with disabled caption auto-detection
     */
    NO_DETECT,

    /**
     * Load recent project
     */
    LOAD_RECENT
}