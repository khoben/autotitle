package com.khoben.autotitle.huawei.ui.overlay

data class OverlayDataMapper(
    var startTime: Long = 0,
    var endTime: Long = 0,
    var timestamp: Long = 0,
    var text: String = "",
    var isSelected: Boolean = false
) : Comparable<OverlayDataMapper> {
    override fun compareTo(other: OverlayDataMapper): Int {
        return (this.startTime - other.startTime).toInt()
    }
}