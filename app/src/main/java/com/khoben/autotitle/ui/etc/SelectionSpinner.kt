package com.khoben.autotitle.ui.etc

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.animation.addListener
import com.khoben.autotitle.databinding.DropdownLayoutBinding
import com.khoben.autotitle.model.LanguageItem
import com.minibugdev.sheetselection.SheetSelection
import com.minibugdev.sheetselection.SheetSelectionItem


class SelectionSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: DropdownLayoutBinding =
        DropdownLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private var sheetSelection: SheetSelection.Builder? = null

    init {
        setOnClickListener {
            showBottomSheetSelection()
        }
    }

    private fun showBottomSheetSelection() {
        sheetSelection?.show()
    }

    fun init(label: String, items: List<LanguageItem>? = null) {
        binding.txtDropdownLabel.text = label
        if (!items.isNullOrEmpty()) {

            sheetSelection = SheetSelection.Builder(context)
                .title("Language")
                .items(items.map {
                    SheetSelectionItem(it.key, it.value, it.icon)
                })
                .showDraggedIndicator(true)
                .searchEnabled(true)
                .searchNotFoundText("Nothing!!")
                .onItemClickListener { item, position ->
                    setSelectedText(item.value)
                    sheetSelection?.selectedPosition(position)
                }

            setSelectedText(items[0].value)
        }
    }

    private fun setSelectedText(text: String) {
        binding.txtDropdownValue.text = text.split(' ')[0]
    }

    /**
     * Collapse or expand the dropdown
     *
     * if [state] equals to true then expand the dropdown and vice versa
     *
     * @param state Boolean
     */
    fun toggleCollapseExpand(state: Boolean) {
        val view = binding.root
        val alphaAnimator: ObjectAnimator
        val translationAnimator: ObjectAnimator
        if (!state) {
            translationAnimator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0F, -100F)
            )
            alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        } else {
            alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            translationAnimator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -100F, 0F)
            )
        }
        AnimatorSet().apply {
            playTogether(
                alphaAnimator,
                translationAnimator
            )
            addListener(
                onEnd = {
                    if (!state) view.visibility = View.INVISIBLE
                },
                onStart = {
                    if (state) view.visibility = View.VISIBLE
                }
            )
        }.start()
    }

    /**
     * Set visibility depending on [state] value:
     *
     *  true -- [View.VISIBLE]
     *
     *  false -- [View.INVISIBLE]
     *
     * @param state Boolean
     */
    fun setVisibility(state: Boolean) {
        val visibility = if (state) View.VISIBLE else View.INVISIBLE
        binding.root.visibility = visibility
    }
}