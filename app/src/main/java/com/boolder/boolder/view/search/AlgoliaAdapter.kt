package com.boolder.boolder.view.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.algolia.instantsearch.core.hits.HitsView
import com.boolder.boolder.data.network.model.AreaRemote
import com.boolder.boolder.data.network.model.ProblemRemote
import com.boolder.boolder.databinding.SearchResultItemBinding

interface BaseObject
abstract class BaseViewHolder(view: View) : ViewHolder(view)

class AlgoliaAdapter : RecyclerView.Adapter<BaseViewHolder>(), HitsView<BaseObject> {

    override fun getItemViewType(position: Int): Int {
        val a: BaseObject = getItem(position)
        return if (a is AreaRemote) 1 else 0
    }

    inner class AreaViewHolder4(private val binding: SearchResultItemBinding) : BaseViewHolder(binding.root) {
        fun bind(area: AreaRemote) {
            binding.title.text = area.name
        }
    }

    inner class ProblemViewHolder4(private val binding: SearchResultItemBinding) : BaseViewHolder(binding.root) {
        fun bind(problem: ProblemRemote) {
            binding.title.text = problem.name
        }
    }

    private var items: List<BaseObject> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == 0) {
            ProblemViewHolder4(SearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else AreaViewHolder4(SearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is ProblemViewHolder4) {
            holder.bind(getItem(position) as ProblemRemote)
        } else {
            (holder as AreaViewHolder4).bind(getItem(position) as AreaRemote)
        }
    }

    fun getItem(position: Int): BaseObject {
        return items[position]
    }

    override fun getItemCount(): Int = items.size

    override fun setHits(hits: List<BaseObject>) {
        items = hits
        notifyDataSetChanged()
    }
}