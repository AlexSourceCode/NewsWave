package com.example.newswave.presentation.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.newswave.R
import com.example.newswave.databinding.NewsItemBinding
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.utils.DateUtils
import com.example.newswave.utils.NetworkUtils
import com.example.newswave.utils.TextUtils
import com.squareup.picasso.Picasso

/**
 * Адаптер для отображения списка новостей в RecyclerView
 */
class NewsListAdapter(
    private val context: Context
) : ListAdapter<NewsItemEntity, NewsListAdapter.NewsListViewHolder>(NewsListDiffCallback) {

    // Обработчик нажатия на новость
    var onNewsClickListener: ((NewsItemEntity) -> Unit)? = null

    // Обработчик загрузки дополнительной страницы новостей
    var onLoadMoreListener: (() -> Unit)? = null

    // Обработчик загрузки новостей
    var onLoadListener: (() -> Unit)? = null

    // Флаг, показывать ли кнопку повторной загрузки в случае ошибки
    var shouldHideRetryButton: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsListViewHolder {
        val binding = NewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsListViewHolder(binding)
    }

    class NewsListViewHolder(
        val binding: NewsItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: NewsListViewHolder, position: Int) {
        val news = getItem(position) // Получает текущую новость

        // Вызывает обработчик загрузки новостей
        onLoadListener?.invoke()
        with(holder.binding) {
            with(news) {
                // Устанавливает данные в соответствующие элементы UI
                tvText.text = TextUtils.sentenceDivision(news.text)
                tvTitle.text = news.title
                tvDate.text = DateUtils.dateFormat(context, news.publishDate)

                // Загружает изображение, если есть доступ в интернет
                try {
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        Picasso.get()
                            .load(image)
                            .resize(800, 600)
                            .into(ivImage)
                    } else {
                        ivImage.setImageResource(R.drawable.error_placeholder) // Показываем изображение по умолчанию, если нет интернета
                    }
                } catch (e: Exception) {
                    Log.d("CheckPathImage", e.message.toString())
                }

            }
        }

        // Обработчик нажатия на кнопку для просмотра новости
        holder.binding.btReadDetail.setOnClickListener {
            onNewsClickListener?.invoke(news)
        }

        // Если это последний элемент списка, вызываем обработчик загрузки дополнительных новостей
        if (position == itemCount - 1) {
            onLoadMoreListener?.invoke()
        }

        // Если это последний элемент списка и кнопка повторной загрузки не скрыта, показываем кнопку для повторной загрузки
        if ((position == itemCount - 1) && (!shouldHideRetryButton)) {
            holder.binding.cvRetryLoadingMore.visibility = View.VISIBLE
            holder.binding.tvMoreRetry.setOnClickListener {
                onLoadMoreListener?.invoke()
            }
        } else {
            holder.binding.cvRetryLoadingMore.visibility = View.GONE // Скрываем кнопку повторной загрузки
        }
    }

    // Метод для обновления списка с показом кнопки повторной загрузки
    fun submitListWithLoadMore(list: List<NewsItemEntity>?, commitCallback: Runnable?) {
        shouldHideRetryButton = false
        super.submitList(list, commitCallback)
    }
}