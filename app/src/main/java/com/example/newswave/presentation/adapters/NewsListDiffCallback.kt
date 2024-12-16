package com.example.newswave.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.newswave.domain.entities.NewsItemEntity

// DiffCallback для сравнения элементов списка новостей
object NewsListDiffCallback: DiffUtil.ItemCallback<NewsItemEntity>() {
    override fun areItemsTheSame(oldItem: NewsItemEntity, newItem: NewsItemEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NewsItemEntity, newItem: NewsItemEntity): Boolean {
        return oldItem == newItem
    }
}