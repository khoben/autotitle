package com.khoben.autotitle.ui.popup.textoverlayeditor

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TextOverlayEditorViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragmentList = arrayListOf<Fragment>()

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    /**
     * Adds [fragment] with [idx] to viewpager adapter
     *
     * @param fragment Fragment
     * @param idx Index
     */
    fun add(fragment: Fragment, idx: Int) {
        fragmentList.add(idx, fragment)
    }

    /**
     * Removes fragment with [idx] from viewpager adapter
     *
     * @param idx Index of fragment to remove
     */
    fun remove(idx: Int) {
        fragmentList.removeAt(idx)
    }

    /**
     * Get fragment with ID equals to [idx]
     *
     * @param idx Fragment ID
     * @return Fragment
     */
    fun getFragment(idx: Int): Fragment {
        return fragmentList[idx]
    }
}