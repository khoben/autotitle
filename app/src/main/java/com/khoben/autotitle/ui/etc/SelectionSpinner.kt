package com.khoben.autotitle.ui.etc

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.core.animation.addListener
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.DropdownLayoutBinding


class SelectionSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: DropdownLayoutBinding =
        DropdownLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        if (binding.txtDropdownValue.text.isNullOrBlank()) {
            binding.txtDropdownValue.text = context.getString(R.string.language_selection_notselected)
        }
    }


    fun setLabel(label: String) {
        binding.txtDropdownLabel.text = label
    }

    fun setSelectedValue(text: String) {
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

    fun errorLanguageSelection() {
        startAnimation(shakeError())
    }

    private fun shakeError(): TranslateAnimation {
        val shake = TranslateAnimation(0F, 10F, 0F, 0F)
        shake.duration = 500
        shake.interpolator = CycleInterpolator(7F)
        return shake
    }

}