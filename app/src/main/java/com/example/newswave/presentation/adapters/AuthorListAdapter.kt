package com.example.newswave.presentation.adapters

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.example.newswave.R
import com.example.newswave.databinding.AuthorItemBinding
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.utils.StringUtils

class AuthorListAdapter(private val context: Context): ListAdapter<AuthorItemEntity, AuthorListViewHolder>(AuthorListDiffCallback) {

    var onAuthorClickSubscription: ((String) -> Unit)? = null
    var onAuthorClickNews: ((String) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorListViewHolder {
        val binding = AuthorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false) // temp
        return AuthorListViewHolder(binding) //temp
    }

    override fun onBindViewHolder(holder: AuthorListViewHolder, position: Int) {
        val author = getItem(position).author
        val context = holder.itemView.context

        holder.binding.tvAuthor.text = StringUtils.truncateText(author, 28)
        holder.binding.btSubscription.text = context.getString(R.string.subscribed)
        if ("RU" == currentLanguage()){
            holder.binding.btSubscription.setBackgroundResource(R.drawable.button_subscribed_rus_in_authors_list)
        } else {
            holder.binding.btSubscription.setBackgroundResource(R.drawable.button_subscribed_in_authors_list)
        }


        holder.binding.btSubscription.setOnClickListener {
            onAuthorClickSubscription?.invoke(author)
        }

        holder.itemView.setOnClickListener {
            onAuthorClickNews?.invoke(author)
        }
    }

    private fun currentLanguage(): String {
        val currentLocale = context.resources.configuration.locales[0]
        val currentLanguage = currentLocale.language
        val currentCountry = currentLocale.country
        return currentCountry
    }
}