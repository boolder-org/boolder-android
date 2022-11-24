package com.boolder.boolder.view.search

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.boolder.boolder.R
import com.boolder.boolder.databinding.SearchResultAreaItemBinding
import com.boolder.boolder.databinding.SearchResultItemBinding
import com.boolder.boolder.databinding.SearchResultProblemItemBinding
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.CircuitColor.WHITE
import com.boolder.boolder.domain.model.Problem

interface BaseObject
data class CategoryHeader(val titleId: Int) : BaseObject
abstract class BaseViewHolder(view: View) : ViewHolder(view)

class AlgoliaAdapter(
    private val onProblemClick: (Problem) -> Unit,
    private val onAreaClick: (Area) -> Unit
) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CategoryHeader -> 0
            is Problem -> 1
            else -> 2
        }
    }

    inner class CategoryHeaderViewHolder(private val binding: SearchResultItemBinding) : BaseViewHolder(binding.root) {
        fun bind(header: CategoryHeader) {
            binding.title.text = binding.root.context.getString(header.titleId)

        }
    }

    inner class AreaViewHolder(private val binding: SearchResultAreaItemBinding) : BaseViewHolder(binding.root) {
        fun bind(area: Area) {
            binding.areaName.text = area.name
            binding.root.setOnClickListener { onAreaClick(area) }
        }
    }

    inner class ProblemViewHolder(private val binding: SearchResultProblemItemBinding) : BaseViewHolder(binding.root) {
        fun bind(problem: Problem) {
            binding.problemName.text = problem.name
            binding.problemGrade.text = problem.grade
            binding.circuitNumber.text = problem.circuitNumber

            val textColor = if (problem.circuitColorSafe == WHITE) {
                ColorStateList.valueOf(Color.BLACK)
            } else ColorStateList.valueOf(Color.WHITE)

            binding.circuitNumber.setTextColor(textColor)
            binding.circuitColor.backgroundTintList = ColorStateList.valueOf(problem.drawColor(binding.root.context))

            binding.problemArea.text = problem.areaName

            binding.root.setOnClickListener { onProblemClick(problem) }
        }
    }

    private val problemHeader = CategoryHeader(R.string.category_problem)
    private val areaHeader = CategoryHeader(R.string.category_area)
    var items: MutableList<BaseObject> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> CategoryHeaderViewHolder(SearchResultItemBinding.inflate(inflater, parent, false))
            1 -> ProblemViewHolder(SearchResultProblemItemBinding.inflate(inflater, parent, false))
            else -> AreaViewHolder(SearchResultAreaItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is CategoryHeaderViewHolder -> {
                holder.bind(getItem(position) as CategoryHeader)
            }
            is ProblemViewHolder -> {
                holder.bind(getItem(position) as Problem)
            }
            else -> {
                (holder as AreaViewHolder).bind(getItem(position) as Area)
            }
        }
    }

    private fun getItem(position: Int): BaseObject {
        return items[position]
    }

    override fun getItemCount(): Int = items.size

    fun setHits(hits: List<BaseObject>) {
        items.clear()
        if (hits.any { it is Area }) {
            items.add(areaHeader)
            items.addAll(hits.filterIsInstance<Area>())
        }

        if (hits.any { it is Problem }) {
            items.add(problemHeader)
            items.addAll(hits.filterIsInstance<Problem>())
        }

        notifyDataSetChanged()
    }
}