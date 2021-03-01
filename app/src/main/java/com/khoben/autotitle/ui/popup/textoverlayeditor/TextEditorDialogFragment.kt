package com.khoben.autotitle.ui.popup.textoverlayeditor

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.EditText
import android.widget.PopupWindow
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.AddTextDialogBinding
import com.khoben.autotitle.util.ViewUtils.focusAndShowKeyboard
import com.khoben.autotitle.util.ViewUtils.hideKeyboard
import timber.log.Timber
import kotlin.math.max


class TextEditorDialogFragment : DialogFragment() {

    private var mAddTextEditText: EditText? = null
    private var mColorCode = 0

    private lateinit var binding: AddTextDialogBinding

    private var mTextEditorEvent: TextEditorEvent? = null

    private val keyboardTabIndex = 0
    private val keyboardOpenedHeightThreshold = 100

    interface TextEditorEvent {
        fun onDone(inputText: String?, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        // Fullscreen mode
        dialog?.window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            /**
             * Set fixed height as parent activity height to allow the keyboard overlaps TabLayout
             * reference: https://stackoverflow.com/questions/55803448/how-to-prevent-dialogfragment-from-resize
             **/
//            (activity?.window!!.decorView.findViewById<View>(android.R.id.content).height)
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Allow the keyboard to overlap content without any effects on it
//        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddTextDialogBinding.inflate(inflater, container, false)
        setupViewPager()
        setupKeyboardEventListener()
        return binding.root
    }

    override fun onDestroyView() {
        keyboardHeightListener?.dismiss()
        super.onDestroyView()
    }

    private var keyboardHeightListener: KeyboardHeightListener? = null
    private fun setupKeyboardEventListener() {
        keyboardHeightListener = KeyboardHeightListener(requireActivity())
        keyboardHeightListener!!.init(viewLifecycleOwner)
    }

    private val icons = listOf<@DrawableRes Int>(
        R.drawable.keyboard_icon_24dp,
        R.drawable.font_icon_24dp,
        R.drawable.align_left_icon_24dp,
        R.drawable.color_pallete_icon_24dp
    )

    private fun findViewPagerFragmentAtPosition(
        fragmentManager: FragmentManager,
        position: Int
    ): Fragment? {
        return fragmentManager.findFragmentByTag("f$position")
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = TextOverlayEditorViewPagerAdapter(this).apply {
            add(KeyboardInputFragment(), 0)
            add(TextFontChangeFragment(), 1)
            add(TextAlignChangeFragment(), 2)
            add(ColorChangeFragment(), 3)
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setIcon(icons[position])
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == keyboardTabIndex) { // keyboard
                    mAddTextEditText?.focusAndShowKeyboard()
                } else {
                    if (binding.viewPager.height == 0) { // restore after hide on back pressed
                        binding.viewPager.layoutParams =
                            (binding.viewPager.layoutParams).also { lp ->
                                lp.height = keyboardHeight
                            }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                if (tab?.position == keyboardTabIndex) { // keyboard
                    mAddTextEditText?.hideKeyboard()
                    mAddTextEditText?.clearFocus()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (tab?.position == keyboardTabIndex) { // keyboard
                    mAddTextEditText?.focusAndShowKeyboard()
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAddTextEditText = binding.addTextEditText.apply {
            setText(requireArguments().getString(EXTRA_INPUT_TEXT))
        }
        mColorCode = requireArguments().getInt(EXTRA_COLOR_CODE)
        mAddTextEditText!!.setTextColor(mColorCode)

        binding.colorPickerBtn.setOnClickListener {
            ColorPickerDialogBuilder
                .with(requireContext())
                .noSliders()
                .setTitle(getString(R.string.select_color_title))
                .initialColor(mColorCode)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(8)
                .setPositiveButton(
                    android.R.string.ok
                ) { dialog, selectedColor, _ ->
                    dialog.dismiss()
                    mColorCode = selectedColor
                    mAddTextEditText!!.setTextColor(selectedColor)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .build()
                .show()
        }

        binding.addTextColorPickerRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            adapter = activity?.let {
                ColorPickerAdapter(it).apply {
                    onColorPickerClickListener = object :
                        ColorPickerAdapter.OnColorPickerClickListener {
                        override fun onColorPickerClickListener(colorCode: Int) {
                            mColorCode = colorCode
                            mAddTextEditText!!.setTextColor(colorCode)
                        }
                    }
                }
            }
        }

        // show keyboard
        mAddTextEditText!!.focusAndShowKeyboard()

        //Make a callback on activity when user is done with text editing
        binding.addTextDoneTv.setOnClickListener {
            dismissAllowingStateLoss()
            val inputText = mAddTextEditText!!.text.toString()
            if (!TextUtils.isEmpty(inputText)) {
                mTextEditorEvent?.onDone(inputText, mColorCode)
            }
        }
    }

    fun setOnTextEditorListener(textEditorEvent: TextEditorEvent?) {
        mTextEditorEvent = textEditorEvent
    }

    companion object {
        private val TAG: String = TextEditorDialogFragment::class.java.simpleName
        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_COLOR_CODE = "extra_color_code"

        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String? = "",
            @ColorInt colorCode: Int = ContextCompat.getColor(appCompatActivity, R.color.white)
        ): TextEditorDialogFragment {
            return TextEditorDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_INPUT_TEXT, inputText)
                    putInt(EXTRA_COLOR_CODE, colorCode)
                }
                show(appCompatActivity.supportFragmentManager, TAG)
            }
        }
    }

    inner class KeyboardHeightListener(private val fragmentActivity: FragmentActivity) :
        PopupWindow(),
        ViewTreeObserver.OnGlobalLayoutListener {
        private var rootView = View(fragmentActivity)
        private val keyboardHeightCalcRect = Rect()

        init {
            contentView = rootView
            setBackgroundDrawable(ColorDrawable(0))
            width = 0
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        fun init(lifecycleOwner: LifecycleOwner) {
            Timber.d("init")
            rootView.viewTreeObserver.addOnGlobalLayoutListener(this@KeyboardHeightListener)
            lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    lifecycleOwner.lifecycle.removeObserver(this)
                    this@KeyboardHeightListener.onDestroy()
                }
            })
            if (!isShowing) {
                val view = fragmentActivity.window.decorView
                view.post { showAtLocation(view, Gravity.NO_GRAVITY, 0, 0) }
            }
        }

        override fun onGlobalLayout() {
            // calc keyboard height
            rootView.getWindowVisibleDisplayFrame(keyboardHeightCalcRect)
            var notchHeight = 0
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                notchHeight += fragmentActivity.window.decorView.rootWindowInsets.displayCutout?.safeInsetTop
                    ?: 0
            } else {
                val resourceID =
                    fragmentActivity.resources.getIdentifier(
                        "status_bar_height",
                        "dimen",
                        "android"
                    )
                if (resourceID > 0) {
                    notchHeight += fragmentActivity.resources.getDimensionPixelSize(resourceID)
                }
            }

            val heightDiff = max(0, rootView.height + notchHeight - keyboardHeightCalcRect.bottom)
            if (heightDiff > keyboardOpenedHeightThreshold) {
                onKeyboardShow(heightDiff)
            } else {
                onKeyboardHide(heightDiff)
            }
            onKeyboardHeightChanged(heightDiff)
        }

        fun onDestroy() {
            Timber.d("onDestroy")
            rootView.viewTreeObserver?.removeOnGlobalLayoutListener(this)
        }
    }

    private fun onKeyboardHide(heightDiff: Int) {
        Timber.d("Keyboard Closed.\nKeyboard height: $heightDiff")
    }

    private var keyboardHeight = -1
    private fun onKeyboardShow(heightDiff: Int) {
        Timber.d("Keyboard Opened.\nKeyboard height: $heightDiff")
        if (keyboardHeight == -1) {
            keyboardHeight = heightDiff
            // For all fragments set height equals to [keyboardHeight]
            for (i in 0..binding.viewPager.adapter!!.itemCount) {
                findViewPagerFragmentAtPosition(childFragmentManager, i)?.requireView()
                    ?.apply {
                        layoutParams = (layoutParams).also { lp -> lp.height = keyboardHeight }
                    }
            }
        }
        /**
         * Changes selected tab to tab with id equals to [keyboardTabIndex]
         * if keyboard appears
         **/
        val curSelected = binding.tabLayout.selectedTabPosition
        if (curSelected == -1) return
        if (curSelected != keyboardTabIndex) {
            binding.tabLayout.getTabAt(keyboardTabIndex)?.select()
        }
    }

    private fun onKeyboardHeightChanged(heightDiff: Int) {
        val curSelected = binding.tabLayout.selectedTabPosition
        if (curSelected == keyboardTabIndex) {
            val height = if (heightDiff > keyboardOpenedHeightThreshold) keyboardHeight else 0
            findViewPagerFragmentAtPosition(childFragmentManager, keyboardTabIndex)
                ?.requireView()
                ?.apply {
                    layoutParams = (layoutParams).also { lp -> lp.height = height }
                    binding.viewPager.layoutParams =
                        (binding.viewPager.layoutParams).also { lp ->
                            lp.height = height
                        }
                }
        }
    }
}