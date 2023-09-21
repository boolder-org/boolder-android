package com.boolder.boolder.view.search.viewholder

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.boolder.boolder.databinding.SearchResultProblemItemBinding
import com.boolder.boolder.domain.model.CircuitColor
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.utils.extension.inflater
import com.boolder.boolder.view.search.BaseViewHolder

class ProblemViewHolder private constructor(
    private val binding: SearchResultProblemItemBinding,
    private val onProblemClicked: (Problem) -> Unit,
) : BaseViewHolder(binding.root) {

    fun bind(problem: Problem) {
        binding.apply {
            root.setOnClickListener { onProblemClicked(problem) }

            problemName.text = problem.name
            problemArea.text = problem.areaName
            problemGrade.text = problem.grade
            circuitNumber.text = problem.circuitNumber

            val textColor = if (problem.circuitColorSafe == CircuitColor.WHITE) {
                Color.BLACK
            } else {
                Color.WHITE
            }

            if (problem.state == 0){
                tick.visibility = View.VISIBLE
            } else if (problem.state == 1){
                save.visibility = View.VISIBLE
            }

            circuitNumber.setTextColor(ColorStateList.valueOf(textColor))
            circuitColor.backgroundTintList =
                ColorStateList.valueOf(problem.getColor(root.context))
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onProblemClicked: (Problem) -> Unit,
        ) = ProblemViewHolder(
            binding = SearchResultProblemItemBinding.inflate(parent.inflater, parent, false),
            onProblemClicked = onProblemClicked,
        )
    }
}
