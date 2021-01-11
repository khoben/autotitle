package com.khoben.autotitle.ui.overlay

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE)
@IntDef(value = [TEXT, IMAGE])
@Retention(AnnotationRetention.SOURCE)
annotation class OverlayType

const val TEXT = 0
const val IMAGE = 1