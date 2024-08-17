package com.example.newswave.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.newswave.domain.NewsInfo

object NewsListDiffCallback: DiffUtil.ItemCallback<NewsInfo>() {
    override fun areItemsTheSame(oldItem: NewsInfo, newItem: NewsInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NewsInfo, newItem: NewsInfo): Boolean {
        return oldItem == newItem
    }
}