package com.example.newswave.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.newswave.databinding.NewsItemBinding
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.utils.DateUtils
import com.example.newswave.utils.TextUtils
import com.squareup.picasso.Picasso

class NewsListAdapter(
    private val context: Context
) : ListAdapter<NewsItemEntity, NewsListViewHolder>(NewsListDiffCallback) {

    var onNewsClickListener: ((NewsItemEntity) -> Unit)? = null
    var onLoadMoreListener: (() -> Unit)? = null
    var onLoadListener: (() -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsListViewHolder {
        val binding = NewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsListViewHolder, position: Int) {
        val news = getItem(position)
        onLoadListener?.invoke()
        with(holder.binding) {
            with(news) {
                tvText.text = TextUtils.sentenceDivision(news.text)
                tvTitle.text = news.title
                tvDate.text = DateUtils.dateFormat(context, news.publishDate)
                Picasso.get()
                    .load(image)
                    .resize(800, 600)
                    .into(ivImage)
            }
        }

        holder.binding.btReadDetail.setOnClickListener {
            onNewsClickListener?.invoke(news)
        }
//        if ( position == itemCount - 10){
//            onLoadMoreListener?.invoke()
//        }
    }
    fun scrollToFirst() {
    }
}