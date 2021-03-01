package com.khoben.autotitle.ui.popup.textoverlayeditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.khoben.autotitle.databinding.FragmentTextAlignBinding

class TextAlignChangeFragment : Fragment() {
    private lateinit var binding: FragmentTextAlignBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTextAlignBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }
}