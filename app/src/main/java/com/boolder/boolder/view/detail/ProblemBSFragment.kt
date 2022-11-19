package com.boolder.boolder.view.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.boolder.boolder.R.layout
import com.boolder.boolder.databinding.BottomSheetBinding
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.domain.model.Topo
import com.boolder.boolder.utils.viewBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProblemBSFragment : BottomSheetDialogFragment() {

    companion object {
        private const val PROBLEM = "problem"
        private const val TOPO = "topo"
        fun newInstance(problem: Problem, topo: Topo) = ProblemBSFragment()
            .apply {
                arguments = bundleOf(PROBLEM to problem, TOPO to topo)
            }
    }

    private val binding: BottomSheetBinding by viewBinding(BottomSheetBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val problem = requireArguments().getParcelable<Problem>(PROBLEM)
            ?: error("No Problem in arguments")

        val topo = requireArguments().getParcelable<Topo>(TOPO)
            ?: error("No Topo in arguments")

        Glide.with(view)
            .load(topo.url)
            .into(binding.picture)

        binding.title.text = problem.name

    }
}