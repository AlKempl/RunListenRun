package com.alkempl.rlr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alkempl.rlr.R
import com.alkempl.rlr.databinding.FragmentLipsumBinding

class LipsumFragment : Fragment() {

    private lateinit var binding: FragmentLipsumBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLipsumBinding.inflate(inflater, container, false)
        return binding.root
    }
}