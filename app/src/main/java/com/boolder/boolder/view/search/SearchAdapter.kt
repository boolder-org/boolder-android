package com.boolder.boolder.view.search

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Problem
import com.boolder.boolder.view.search.viewholder.AreaViewHolder
import com.boolder.boolder.view.search.viewholder.CategoryHeaderViewHolder
import com.boolder.boolder.view.search.viewholder.ProblemViewHolder

interface BaseObject
data class CategoryHeader(val titleId: Int) : BaseObject
abstract class BaseViewHolder(view: View) : ViewHolder(view)

class SearchAdapter(
    private val onProblemClicked: (Problem) -> Unit,
    private val onAreaClicked: (Area) -> Unit
) : ListAdapter<BaseObject, BaseViewHolder>(SearchAdapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        when (viewType) {
            VIEW_TYPE_HEADER -> CategoryHeaderViewHolder.create(parent)
            VIEW_TYPE_PROBLEM -> ProblemViewHolder.create(parent, onProblemClicked)
            VIEW_TYPE_AREA -> AreaViewHolder.create(parent, onAreaClicked)
            else -> throw IllegalArgumentException("Unsupported view type")
        }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is CategoryHeaderViewHolder -> holder.bind(getItem(position) as CategoryHeader)
            is ProblemViewHolder -> holder.bind(getItem(position) as Problem)
            is AreaViewHolder -> holder.bind(getItem(position) as Area)
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is CategoryHeader -> VIEW_TYPE_HEADER
            is Problem -> VIEW_TYPE_PROBLEM
            is Area -> VIEW_TYPE_AREA
            else -> throw IllegalArgumentException("Unsupported item")
        }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_PROBLEM = 1
        private const val VIEW_TYPE_AREA = 2
    }
}
