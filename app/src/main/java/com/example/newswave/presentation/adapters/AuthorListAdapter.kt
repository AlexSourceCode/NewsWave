package com.example.newswave.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.example.newswave.R
import com.example.newswave.databinding.AuthorItemBinding
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.utils.StringUtils

class AuthorListAdapter: ListAdapter<AuthorItemEntity, AuthorListViewHolder>(AuthorListDiffCallback) {

    var onAuthorClickSubscription: ((String) -> Unit)? = null
    var onAuthorClickNews: ((String) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorListViewHolder {
        val binding = AuthorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false) // temp
        return AuthorListViewHolder(binding) //temp
    }

    override fun onBindViewHolder(holder: AuthorListViewHolder, position: Int) {
        val author = getItem(position).author
        val context = holder.itemView.context

        holder.binding.tvAuthor.text = StringUtils.truncateText(author, 30)
        holder.binding.btSubscription.text = context.getString(R.string.subscribed)
        holder.binding.btSubscription.setBackgroundResource(R.drawable.button_subscribed)
        holder.binding.btSubscription.setTextColor(ContextCompat.getColor(context, R.color.white))


        holder.binding.btSubscription.setOnClickListener {
            onAuthorClickSubscription?.invoke(author)
        }

        holder.itemView.setOnClickListener {
            onAuthorClickNews?.invoke(author)
        }
    }
}