package com.boolder.boolder.view.search

import androidx.recyclerview.widget.DiffUtil
import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.domain.model.Problem

class SearchAdapterDiffCallback : DiffUtil.ItemCallback<BaseObject>() {

    override fun areItemsTheSame(oldItem: BaseObject, newItem: BaseObject): Boolean {
        if (oldItem is CategoryHeader && newItem is CategoryHeader)
            return oldItem == newItem

        if (oldItem is Problem && newItem is Problem)
            return oldItem.id == newItem.id

        if (oldItem is Area && newItem is Area)
            return oldItem.id == newItem.id

        return false
    }

    override fun areContentsTheSame(oldItem: BaseObject, newItem: BaseObject): Boolean {
        if (oldItem is CategoryHeader && newItem is CategoryHeader)
            return oldItem == newItem

        if (oldItem is Problem && newItem is Problem)
            return oldItem == newItem

        if (oldItem is Area && newItem is Area)
            return oldItem == newItem

        return false
    }
}
