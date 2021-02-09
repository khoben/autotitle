package com.khoben.autotitle.common

import android.text.Layout
import android.widget.TextView

/**
 * Email: chjie.jaeger@gmail.com
 *
 * GitHub: https://github.com/laobie
 */
object TextViewLayoutUtils {

    private fun isEndOfLineOffset(layout: Layout, offset: Int): Boolean {
        return offset > 0 && layout.getLineForOffset(offset) == layout.getLineForOffset(offset - 1) + 1
    }

    fun getHysteresisOffset(textView: TextView, x: Int, y: Int, previousOffset: Int): Int {
        var previousOffset = previousOffset
        val layout = textView.layout ?: return -1
        var line = layout.getLineForVertical(y)
        val x = x - textView.left - textView.paddingLeft

        // The "HACK BLOCK"S in this function is required because of how Android Layout for
        // TextView works - if 'offset' equals to the last character of a line, then
        //
        // * getLineForOffset(offset) will result the NEXT line
        // * getPrimaryHorizontal(offset) will return 0 because the next insertion point is on the next line
        // * getOffsetForHorizontal(line, x) will not return the last offset of a line no matter where x is
        // These are highly undesired and is worked around with the HACK BLOCK
        //
        // @see Moon+ Reader/Color Note - see how it can't select the last character of a line unless you move
        // the cursor to the beginning of the next line.
        //
        ////////////////////HACK BLOCK////////////////////////////////////////////////////
        if (isEndOfLineOffset(layout, previousOffset)) {
            // we have to minus one from the offset so that the code below to find
            // the previous line can work correctly.
            val left = layout.getPrimaryHorizontal(previousOffset - 1).toInt()
            val right = layout.getLineRight(line).toInt()
            val threshold = (right - left) / 2 // half the width of the last character
            if (x > right - threshold) {
                previousOffset -= 1
            }
        }
        ///////////////////////////////////////////////////////////////////////////////////
        val previousLine = layout.getLineForOffset(previousOffset)
        val previousLineTop = layout.getLineTop(previousLine)
        val previousLineBottom = layout.getLineBottom(previousLine)
        val hysteresisThreshold = (previousLineBottom - previousLineTop) / 2

        // If new line is just before or after previous line and y position is less than
        // hysteresisThreshold away from previous line, keep cursor on previous line.
        if (line == previousLine + 1 && y - previousLineBottom < hysteresisThreshold || line == previousLine - 1 && (previousLineTop
                    - y) < hysteresisThreshold
        ) {
            line = previousLine
        }
        var offset = layout.getOffsetForHorizontal(line, x.toFloat())

        // This allow the user to select the last character of a line without moving the
        // cursor to the next line. (As Layout.getOffsetForHorizontal does not return the
        // offset of the last character of the specified line)
        //
        // But this function will probably get called again immediately, must decrement the offset
        // by 1 to compensate for the change made below. (see previous HACK BLOCK)
        /////////////////////HACK BLOCK///////////////////////////////////////////////////
        if (offset < textView.text.length - 1) {
            if (isEndOfLineOffset(layout, offset + 1)) {
                val left = layout.getPrimaryHorizontal(offset).toInt()
                val right = layout.getLineRight(line).toInt()
                val threshold = (right - left) / 2 // half the width of the last character
                if (x > right - threshold) {
                    offset += 1
                }
            }
        }
        //////////////////////////////////////////////////////////////////////////////////
        return offset
    }
}