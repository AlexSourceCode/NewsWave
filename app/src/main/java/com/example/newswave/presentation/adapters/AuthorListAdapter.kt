package com.example.newswave.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.newswave.R
import com.example.newswave.databinding.AuthorItemBinding
import com.example.newswave.domain.entities.AuthorItemEntity
import com.example.newswave.utils.StringUtils

/**
 * Адаптер для списка авторов
 *
 * Этот адаптер используется для отображения списка авторов в RecyclerView. Он использует ListAdapter
 * для эффективного обновления данных с помощью DiffUtil и реализует функциональность подписки на авторов
 * и перехода к новостям, связанным с ними.
 */
class AuthorListAdapter :
    ListAdapter<AuthorItemEntity, AuthorListAdapter.AuthorListViewHolder>(AuthorListDiffCallback) {

    // Обработчики нажатий для действий подписки и перехода к новостям
    var onAuthorClickSubscription: ((String) -> Unit)? = null
    var onAuthorClickNews: ((String) -> Unit)? = null

    class AuthorListViewHolder(
        val binding: AuthorItemBinding
    ): RecyclerView.ViewHolder(binding.root)

    // Создание нового ViewHolder для элемента списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorListViewHolder {
        val binding =
            AuthorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false) // temp
        return AuthorListViewHolder(binding) //temp
    }

    // Привязка данных к элементу списка
    override fun onBindViewHolder(holder: AuthorListViewHolder, position: Int) {
        // Получение автора из списка данных
        val author = getItem(position).author
        val context = holder.itemView.context

        // Устанавливаем текст имени автора с обрезкой
        holder.binding.tvAuthor.text = StringUtils.truncateText(author, 28)

        // Устанавливаем текст и стиль кнопки подписки
        holder.binding.btSubscription.text = context.getString(R.string.subscribed)

        // Обработчик клика на кнопку подписки
        holder.binding.btSubscription.setOnClickListener {
            onAuthorClickSubscription?.invoke(author)
        }

        // Обработчик клика на элемент списка для перехода к новостям
        holder.itemView.setOnClickListener {
            onAuthorClickNews?.invoke(author)
        }
    }
}