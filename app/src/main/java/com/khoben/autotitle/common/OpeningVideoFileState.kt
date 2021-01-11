package com.khoben.autotitle.common

import androidx.annotation.IntDef

enum class OpeningVideoFileState {
    /**
     *  Failed to open video
     */
    FAILED,
    /**
     * Exceeded time limit (up to [com.khoben.autotitle.App.LIMIT_DURATION_MS] seconds)
     */
    LIMIT,
    /**
     * Video loaded property
     */
    SUCCESS
}