package com.example.newswave.presentation.adapters

import androidx.recyclerview.widget.DiffUtil

/**
 * DiffCallback для сравнения элементов списка
 */
object LanguageListDiffCallBack : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}