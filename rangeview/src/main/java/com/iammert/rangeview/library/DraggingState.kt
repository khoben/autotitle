package com.iammert.rangeview.library

enum class DraggingState {
    /**
     * Dragging left pointer
     */
    DRAGGING_LEFT_TOGGLE,
    /**
     * Dragging right pointer
     */
    DRAGGING_RIGHT_TOGGLE,
    /**
     * Conflict left/right pointers
     */
    DRAGGING_CONFLICT_TOGGLE,
    /**
     * Idle
     */
    NO_DRAGGING,
    /**
     * Dragging end
     */
    DRAGGING_END
}