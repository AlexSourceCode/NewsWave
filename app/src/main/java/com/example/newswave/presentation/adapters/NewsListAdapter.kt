package com.example.newswave.presentation.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.newswave.R
import com.example.newswave.databinding.NewsItemBinding
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.utils.DateUtils
import com.example.newswave.utils.NetworkUtils
import com.example.newswave.utils.TextUtils
import com.squareup.picasso.Picasso

class NewsListAdapter(
    private val context: Context
) : ListAdapter<NewsItemEntity, NewsListViewHolder>(NewsListDiffCallback) {

    var onNewsClickListener: ((NewsItemEntity) -> Unit)? = null
    var onLoadMoreListener: (() -> Unit)? = null
    var onLoadListener: (() -> Unit)? = null

    var shouldHideRetryButton: Boolean = true

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
                Log.d("CheckPathImage", news.title.toString())
                Log.d("CheckPathImage", news.image.toString())
                try {
                    if (NetworkUtils.isNetworkAvailable(context)){
                        Picasso.get()
                            .load(image)
                            .resize(800, 600)
                            .into(ivImage)
                    } else ivImage.setImageResource(R.drawable.error_placeholder)
                } catch (e: Exception){
                    Log.d("CheckPathImage", e.message.toString())
                }

            }
        }

        holder.binding.btReadDetail.setOnClickListener {
            onNewsClickListener?.invoke(news)
        }
        if (position == itemCount - 1) {
            onLoadMoreListener?.invoke()
        }
        if ((position == itemCount - 1) && (!shouldHideRetryButton)) {
            holder.binding.cvRetryLoadingMore.visibility = View.VISIBLE
            holder.binding.tvMoreRetry.setOnClickListener {
                onLoadMoreListener?.invoke()
            }
        }  else{
            holder.binding.cvRetryLoadingMore.visibility = View.GONE
        }
    }

    fun submitListWithLoadMore(list: List<NewsItemEntity>?, commitCallback: Runnable?) {
        shouldHideRetryButton = false
        super.submitList(list, commitCallback)
    }
}