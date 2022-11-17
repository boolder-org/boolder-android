package com.boolder.boolder.view.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boolder.boolder.R
import com.boolder.boolder.databinding.FragmentDiscoveryBinding
import com.boolder.boolder.viewBinding


class DiscoveryFragment : Fragment() {

    private val binding by viewBinding(FragmentDiscoveryBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discovery, container, false)
    }

}