package com.khoben.autotitle.ui.overlay

import java.util.*

data class OverlayDataMapper(
    var startTime: Long = 0,
    var endTime: Long = 0,
    var uuid: UUID,
    var text: String = "",
    var isSelected: Boolean = false,
    var badgeColor: Int = 0,
) : Comparable<OverlayDataMapper> {
    override fun compareTo(other: OverlayDataMapper): Int {
        return (this.startTime - other.startTime).toInt()
    }
}