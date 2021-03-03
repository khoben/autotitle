package com.minibugdev.sheetselection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.minibugdev.sheetselection.databinding.RowEmptyItemBinding
import com.minibugdev.sheetselection.databinding.RowSelectionItemBinding

typealias OnItemSelectedListener = (item: SheetSelectionItem, position: Int) -> Unit

class SheetSelectionAdapter(
    private val source: List<SheetSelectionItem>,
    private val selectedItem: SheetSelectionItem?,
    private val searchNotFoundText: String,
    private val onItemSelectedListener: OnItemSelectedListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var items: List<SheetSelectionItem> = source

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int =
        when (items[position].key) {
            KEY_SEARCH_NOT_FOUND -> R.layout.row_empty_item
            else -> R.layout.row_selection_item
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.row_selection_item -> {
                val binding = RowSelectionItemBinding.inflate(layoutInflater, parent, false)
                ItemViewHolder(binding)
            }
            R.layout.row_empty_item -> {
                val binding = RowEmptyItemBinding.inflate(layoutInflater, parent, false)
                EmptyViewHolder(
                    binding,
                    searchNotFoundText
                )
            }
            else -> throw IllegalAccessException("Item view type doesn't match.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.onBindView(
                item = items[position],
                position = position,
                selected = (items[position] == selectedItem),
                onItemSelectedListener = onItemSelectedListener
            )
        }
    }

    private var recyclerView: EmptyRecyclerView? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (recyclerView is EmptyRecyclerView) this.recyclerView = recyclerView
    }

    fun search(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            recyclerView?.toggleSearchState(true)
            updateItems(source)
        } else {
            val searchResult = source.filter { it.value.contains(keyword, true) }
            if (searchResult.isEmpty()) {
                recyclerView?.toggleSearchState(false)
                updateItems(listOf(SheetSelectionItem(KEY_SEARCH_NOT_FOUND, searchNotFoundText)))
            } else {
                recyclerView?.toggleSearchState(true)
                updateItems(searchResult)
            }
        }
    }

    private fun updateItems(items: List<SheetSelectionItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    class ItemViewHolder(
        private val binding: RowSelectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root){

        fun onBindView(
            item: SheetSelectionItem,
            position: Int,
            selected: Boolean,
            onItemSelectedListener: OnItemSelectedListener?
        ) {
            val selectedIcon = if (selected) R.drawable.ic_check else 0
            binding.textViewItem.setCompoundDrawablesWithIntrinsicBounds(
                item.icon ?: 0,
                0,
                selectedIcon,
                0
            )
            binding.textViewItem.text = item.value


            binding.textViewItem.setOnClickListener {
                onItemSelectedListener?.invoke(item, position)
            }
        }
    }

    class EmptyViewHolder(
        binding: RowEmptyItemBinding, emptyText: String
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.textViewItem.text = emptyText
        }
    }

    companion object {
        private const val KEY_SEARCH_NOT_FOUND = "SheetSelectionAdapter:search_not_found"
    }
}