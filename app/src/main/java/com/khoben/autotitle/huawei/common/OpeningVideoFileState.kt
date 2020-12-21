package com.khoben.autotitle.huawei.common

enum class OpeningVideoFileState {
    /**
     *  Failed to open video
     */
    FAILED,
    /**
     * Exceeded time limit (up to [com.khoben.autotitle.huawei.App.LIMIT_DURATION_MS] seconds)
     */
    LIMIT,
    /**
     * Video loaded property
     */
    SUCCESS
}