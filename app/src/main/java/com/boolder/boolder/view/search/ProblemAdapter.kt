package com.boolder.boolder.view.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.boolder.boolder.data.network.model.ProblemRemote
import com.boolder.boolder.databinding.SearchResultItemBinding
import com.boolder.boolder.view.search.ProblemAdapter.SearchViewHolder

// TODO Understand with either flow or livedata aren't updated on query changes
// Issue open on Github
// https://github.com/algolia/instantsearch-android/issues/374
class ProblemAdapter : PagingDataAdapter<ProblemRemote, SearchViewHolder>(SearchDiffUtil) {

    inner class SearchViewHolder(private val binding: SearchResultItemBinding) : ViewHolder(binding.root) {
        fun bind(problem: ProblemRemote) {
            binding.title.text = problem.name
        }
    }

    object SearchDiffUtil : DiffUtil.ItemCallback<ProblemRemote>() {
        override fun areItemsTheSame(oldItem: ProblemRemote, newItem: ProblemRemote) = oldItem == newItem

        override fun areContentsTheSame(oldItem: ProblemRemote, newItem: ProblemRemote) =
            oldItem.objectID == newItem.objectID

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(SearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}