package com.boolder.boolder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boolder.boolder.R
import com.boolder.boolder.databinding.FragmentSearchBinding
import com.boolder.boolder.viewBinding

class SearchFragment : Fragment() {

    private val binding by viewBinding(FragmentSearchBinding::bind)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editSearch.requestFocus()
    }
}