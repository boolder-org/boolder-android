package com.boolder.boolder.view.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.boolder.boolder.data.network.model.AreaRemote
import com.boolder.boolder.databinding.SearchResultItemBinding
import com.boolder.boolder.view.search.AreaAdapter.AreaViewHolder

// TODO Understand with either flow or livedata aren't updated on query changes
// Issue open on Github
// https://github.com/algolia/instantsearch-android/issues/374
class AreaAdapter : PagingDataAdapter<AreaRemote, AreaViewHolder>(SearchDiffUtil) {

    inner class AreaViewHolder(private val binding: SearchResultItemBinding) : ViewHolder(binding.root) {
        fun bind(area: AreaRemote) {
            binding.title.text = area.name
        }
    }

    object SearchDiffUtil : DiffUtil.ItemCallback<AreaRemote>() {
        override fun areItemsTheSame(oldItem: AreaRemote, newItem: AreaRemote) = oldItem == newItem

        override fun areContentsTheSame(oldItem: AreaRemote, newItem: AreaRemote) = oldItem.objectID == newItem.objectID

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        return AreaViewHolder(SearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: AreaViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

}