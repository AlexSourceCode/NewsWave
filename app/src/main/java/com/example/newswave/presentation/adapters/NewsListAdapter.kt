package com.example.newswave.presentation.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.newswave.databinding.NewsItemBinding
import com.example.newswave.domain.NewsItemEntity
import com.example.newswave.utils.DateAndTextUtils
import com.squareup.picasso.Picasso

class NewsListAdapter(
    private val context: Context
) : ListAdapter<NewsItemEntity, NewsListViewHolder>(NewsListDiffCallback) {

    var onNewsClickListener: ((NewsItemEntity) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsListViewHolder {
        val binding = NewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsListViewHolder, position: Int) {
        val news = getItem(position)
        with(holder.binding) {
            with(news) {
                tvText.text = DateAndTextUtils.sentenceDivision(news.text)
                tvTitle.text = news.title
                tvDate.text = DateAndTextUtils.dateFormat(context, news.publishDate)
                Picasso.get().load(image).into(ivImage)
            }
        }
        holder.binding.btReadDetail.setOnClickListener {
            onNewsClickListener?.invoke(news)
        }
    }



}