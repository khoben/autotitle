package com.minibugdev.sheetselection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_sheet_selection.*


class SheetSelection: BottomSheetDialogFragment() {

    private var selectedItem: SheetSelectionItem? = null
    private var items: List<SheetSelectionItem>? = null
    private var searchNotFoundText: String? = null

    private val adapter by lazy {
        SheetSelectionAdapter(
            source = items!!,
            selectedItem = selectedItem,
            searchNotFoundText = searchNotFoundText ?: "Search not found.",
            onItemSelectedListener = onItemSelectedListener
        )
    }

    private val screenHeight by lazy {
        val statusBarHeight = try {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            resources.getDimensionPixelSize(resourceId)
        } catch (e: Exception) {
            0
        }
        resources.displayMetrics.heightPixels - statusBarHeight
    }

    override fun getTheme(): Int = arguments?.getInt(ARGS_THEME) ?: super.getTheme()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_sheet_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { args ->
            if (args.getBoolean(ARGS_SHOW_DRAGGED_INDICATOR)) {
                draggedIndicator.visibility = View.VISIBLE
            }

            val title = args.getString(ARGS_TITLE)
            if (title.isNullOrEmpty()) {
                textViewTitle.visibility = View.GONE
                textViewTitle.text = null
            } else {
                textViewTitle.visibility = View.VISIBLE
                textViewTitle.text = title
            }

            if (args.getBoolean(ARGS_SEARCH_ENABLED)) {
                buttonSearch.visibility = View.VISIBLE
                buttonSearch.setOnClickListener(onSearchClickListener)
                searchView.setOnCloseListener(onSearchCloseListener)
                searchView.setOnQueryTextListener(onSearchQueryTextListener)
            }

            items = args.getParcelableArrayList(ARGS_ITEMS)
            val selectedPosition = args.getInt(ARGS_SELECTED_POSITION)
            if (selectedPosition >= 0 && items != null && items!!.size > selectedPosition) {
                selectedItem = items?.get(selectedPosition)
            }
            searchNotFoundText = args.getString(ARGS_SEARCH_NOT_FOUND_TEXT)

            recyclerViewSelectionItems.setHasFixedSize(true)
            recyclerViewSelectionItems.adapter = adapter
            recyclerViewSelectionItems.setEmptyView(recyclerViewSelectionEmpty.apply {
                findViewById<TextView>(R.id.recyclerViewSelectionEmpty).text =
                    args.getString(ARGS_SEARCH_NOT_FOUND_TEXT)
            })
        }
    }

    private fun updateSheetHeight(viewHeight: Int) {
        rootLayout.layoutParams = rootLayout.layoutParams
            .apply { height = viewHeight }
    }

    private val onItemSelectedListener: OnItemSelectedListener = { item, position ->
        dismiss()
        onItemClickListener?.invoke(item, position)
    }

    private val onSearchClickListener = View.OnClickListener {
        (dialog as? BottomSheetDialog)?.run {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        updateSheetHeight(screenHeight)
        viewSwitcherHeader.displayedChild = 1
        searchView.isIconified = false
    }

    private val onSearchCloseListener = SearchView.OnCloseListener {
        updateSheetHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
        viewSwitcherHeader.displayedChild = 0
        true
    }

    private val onSearchQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(newText: String?): Boolean {
            adapter.search(newText)
            return true
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            adapter.search(query)
            return true
        }
    }

    class Builder(private val context: Context) {
        private val manager: FragmentManager? = when (context) {
            is FragmentActivity -> context.supportFragmentManager
            is Fragment -> context.requireFragmentManager()
            else -> null
        }

        @StyleRes
        private var themeId: Int = R.style.Theme_SheetSelection
        private var title: String? = null
        private var items: List<SheetSelectionItem> = emptyList()
        private var selectedPosition: Int = NO_SELECT
        private var showDraggedIndicator: Boolean = false
        private var searchEnabled: Boolean = false
        private var searchNotFoundText: String? = null
        private var listener: OnItemSelectedListener? = null

        fun theme(@StyleRes themeId: Int) = apply {
            this.themeId = themeId
        }

        fun title(title: String?) = apply {
            this.title = title
        }

        fun selectedPosition(position: Int) = apply {
            this.selectedPosition = position
        }

        fun items(items: List<SheetSelectionItem>) = apply {
            this.items = items
        }

        fun <T> items(
            source: List<T>,
            mapper: (T) -> SheetSelectionItem
        ) = items(source.map { item -> mapper.invoke(item) })

        fun showDraggedIndicator(show: Boolean) = apply {
            this.showDraggedIndicator = show
        }

        fun searchEnabled(enabled: Boolean) = apply {
            this.searchEnabled = enabled
        }

        fun searchNotFoundText(text: String) = apply {
            this.searchNotFoundText = text
        }

        fun searchNotFoundText(@StringRes textResId: Int) = apply {
            this.searchNotFoundText = context.getString(textResId)
        }

        fun onItemClickListener(listener: OnItemSelectedListener) = apply {
            this.listener = listener
        }

        fun build() = SheetSelection().apply {
            arguments = Bundle()
                .apply {
                    putInt(ARGS_THEME, this@Builder.themeId)
                    putString(ARGS_TITLE, this@Builder.title)
                    putParcelableArrayList(ARGS_ITEMS, ArrayList(this@Builder.items))
                    putInt(ARGS_SELECTED_POSITION, this@Builder.selectedPosition)
                    putBoolean(ARGS_SHOW_DRAGGED_INDICATOR, this@Builder.showDraggedIndicator)
                    putBoolean(ARGS_SEARCH_ENABLED, this@Builder.searchEnabled)
                    putString(ARGS_SEARCH_NOT_FOUND_TEXT, this@Builder.searchNotFoundText)
                }
            onItemClickListener = this@Builder.listener
        }

        fun show() {
            manager?.let {
                build().show(it, "SheetSelection:TAG")
            }
        }
    }

    companion object {
        var onItemClickListener: OnItemSelectedListener? = null

        const val NO_SELECT = -1

        private const val ARGS_THEME = "SheetSelection:ARGS_THEME"
        private const val ARGS_TITLE = "SheetSelection:ARGS_TITLE"
        private const val ARGS_ITEMS = "SheetSelection:ARGS_ITEMS"
        private const val ARGS_SEARCH_NOT_FOUND_TEXT = "SheetSelection:ARGS_SEARCH_NOT_FOUND_TEXT"
        private const val ARGS_SELECTED_POSITION = "SheetSelection:ARGS_SELECTED_POSITION"
        private const val ARGS_SHOW_DRAGGED_INDICATOR = "SheetSelection:ARGS_SHOW_DRAGGED_INDICATOR"
        private const val ARGS_SEARCH_ENABLED = "SheetSelection:ARGS_SEARCH_ENABLED"
    }
}