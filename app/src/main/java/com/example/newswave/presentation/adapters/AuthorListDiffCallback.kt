package com.example.newswave.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.newswave.domain.entities.AuthorItemEntity

/**
 * DiffUtil callback для эффективного сравнения авторов
 */
object AuthorListDiffCallback: DiffUtil.ItemCallback<AuthorItemEntity>() {

    // Проверяет, одинаковы ли два элемента по идентификатору (по автору)
    override fun areItemsTheSame(oldItem: AuthorItemEntity, newItem: AuthorItemEntity): Boolean {
        return oldItem.author == newItem.author
    }
    // Проверяет, одинаково ли содержимое двух элементов
    override fun areContentsTheSame(oldItem: AuthorItemEntity, newItem: AuthorItemEntity): Boolean {
        return oldItem == newItem
    }
}