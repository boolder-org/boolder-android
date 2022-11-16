package com.nicolas.boolder.view.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nicolas.boolder.R
import com.nicolas.boolder.databinding.FragmentMapBinding
import com.nicolas.boolder.viewBinding

class MapFragment : Fragment() {

    private val binding by viewBinding(FragmentMapBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }
}