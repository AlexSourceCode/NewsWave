package com.example.newswave.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.newswave.domain.entity.AuthorItemEntity

object AuthorListDiffCallback: DiffUtil.ItemCallback<AuthorItemEntity>() {
    override fun areItemsTheSame(oldItem: AuthorItemEntity, newItem: AuthorItemEntity): Boolean {
        return oldItem.author == newItem.author
    }

    override fun areContentsTheSame(oldItem: AuthorItemEntity, newItem: AuthorItemEntity): Boolean {
        return oldItem == newItem
    }
}