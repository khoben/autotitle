package com.khoben.autotitle.common

import android.R
import android.content.Context
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.URLSpan
import android.text.style.UpdateAppearance
import kotlin.math.max
import kotlin.math.min

object TextSelectionHelper {
    fun packTextRange(start: Int, end: Int): Long {
        return start.toLong() shl 32 or end.toLong()
    }

    fun unpackTextRangeStart(range: Long): Int {
        return (range shr 32 and 0xFFFFFF).toInt()
    }

    fun unpackTextRangeEnd(range: Long): Int {
        return (range and 0xFFFFFF).toInt()
    }


    fun getWordAt(sequence: CharSequence, start: Int, end: Int): Long {
        var start = start
        var end = end
        start = min(max(start, 0), sequence.length - 1) // length - 1 because of end
        end = min(max(end, start), sequence.length - 1)

        // Check for URL spans
        if (sequence is Spanned) {
            val spans = sequence.getSpans(start, end, URLSpan::class.java)
            if (spans.isNotEmpty()) {
                return packTextRange(sequence.getSpanStart(spans[0]), sequence.getSpanEnd(spans[0]))
            }
        }
        while (start > 0 && Character.isLetterOrDigit(sequence[start - 1])) start--
        while (end < sequence.length && Character.isLetterOrDigit(sequence[end])) end++
        return packTextRange(start, end)
    }

    fun setSelection(context: Context?, sequence: Spannable, start: Int, end: Int) {
        val spans = sequence.getSpans(0, sequence.length, SelectionSpan::class.java)
        val span = if (spans.isNotEmpty()) spans[0] as SelectionSpan else SelectionSpan(
            StyledAttrUtils.getColor(
                context!!,
                R.attr.textColorHighlight
            )
        )
        sequence.setSpan(
            span,
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE or Spannable.SPAN_COMPOSING
        )
    }

    fun removeSelection(sequence: Spannable) {
        val spans = sequence.getSpans(0, sequence.length, SelectionSpan::class.java)
        for (span in spans) sequence.removeSpan(span)
    }


    class SelectionSpan(private val mColor: Int) : CharacterStyle(), UpdateAppearance {
        override fun updateDrawState(tp: TextPaint) {
            tp.bgColor = mColor
        }
    }
}