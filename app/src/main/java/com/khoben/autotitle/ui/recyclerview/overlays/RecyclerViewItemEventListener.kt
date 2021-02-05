package com.khoben.autotitle.ui.recyclerview.overlays

interface RecyclerViewItemEventListener {
    fun onMoveUp(id: Int, start: Int, end: Int, text: String?)
    fun onMoveDown(id: Int, start: Int, end: Int, text: String?)
}