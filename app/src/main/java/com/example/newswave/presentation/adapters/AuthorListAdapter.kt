package com.example.newswave.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.newswave.databinding.AuthorItemBinding
import com.example.newswave.domain.entity.AuthorItemEntity

class AuthorListAdapter: ListAdapter<AuthorItemEntity, AuthorListViewHolder>(AuthorListDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorListViewHolder {
        val binding = AuthorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false) // temp
        return AuthorListViewHolder(binding) //temp
    }

    override fun onBindViewHolder(holder: AuthorListViewHolder, position: Int) {
        val author = getItem(position)
        holder.binding.tvAuthor.text = author.author
        holder.binding.btSubscription.text = "Subscribe"
    }

//    override fun getItemViewType(position: Int): Int {
//        return when (getItem(position)){
//
//        }
//    }
}