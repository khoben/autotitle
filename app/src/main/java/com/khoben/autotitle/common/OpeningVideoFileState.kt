package com.khoben.autotitle.common

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE)
@IntDef(value = [FAILED, LIMIT, SUCCESS])
@Retention(AnnotationRetention.SOURCE)
annotation class OpeningVideoFileState

/**
 *  Failed to open video
 */
const val FAILED = 0

/**
 * Exceeded time limit (up to [com.khoben.autotitle.App.LIMIT_DURATION_MS] seconds)
 */
const val LIMIT = 1

/**
 * Video loaded property
 */
const val SUCCESS = 2