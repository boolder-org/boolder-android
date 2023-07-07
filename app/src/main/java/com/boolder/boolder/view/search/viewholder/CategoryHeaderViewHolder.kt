package com.boolder.boolder.view.search.viewholder

import android.view.ViewGroup
import com.boolder.boolder.databinding.SearchResultItemBinding
import com.boolder.boolder.utils.extension.inflater
import com.boolder.boolder.view.search.BaseViewHolder
import com.boolder.boolder.view.search.CategoryHeader

class CategoryHeaderViewHolder private constructor(
    private val binding: SearchResultItemBinding
) : BaseViewHolder(binding.root) {

    fun bind(header: CategoryHeader) {
        binding.title.apply {
            text = context.getString(header.titleId)
        }
    }

    companion object {
        fun create(parent: ViewGroup) = CategoryHeaderViewHolder(
            binding = SearchResultItemBinding.inflate(parent.inflater, parent, false)
        )
    }
}
