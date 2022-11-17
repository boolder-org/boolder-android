package com.boolder.boolder.view.TrackList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boolder.boolder.R
import com.boolder.boolder.databinding.FragmentTrackListBinding
import com.boolder.boolder.viewBinding

class TrackListFragment : Fragment() {

    private val binding by viewBinding(FragmentTrackListBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_track_list, container, false)
    }
}