package com.example.newswave.presentation.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.newswave.databinding.ListItemBinding
import com.example.newswave.presentation.adapters.LanguageListDiffCallBack


/**
 * Адаптер для отображения списка языков в RecyclerView(SettingsFragment)
 */
class LanguageListAdapter(private val languages: List<String>) :
    ListAdapter<String, LanguageListAdapter.ViewHolder>(
        LanguageListDiffCallBack
    ) {
    // Обрабочик, который проверяет, выбран ли текущий язык
    var currentLanguageChecked: ((String) -> Boolean)? = null
    // Обработчик, который вызывается при нажатии на язык
    var onLanguageClick: ((String) -> Unit)? = null

    class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val language = languages[position] // Получаем текущий язык из списка

        // Устанавливаем текст для текущего элемента
        holder.binding.textItem.text = languages[position]

        // Проверяем, выбран ли текущий язык, и отображаем иконку, если выбран
        if (currentLanguageChecked?.invoke(language) == true) {
            holder.binding.checkedImage.visibility = View.VISIBLE
        } else {
            holder.binding.checkedImage.visibility = View.GONE
        }

        // Обработчик нажатия на элемент списка, передает выбранный язык
        holder.binding.textItem.setOnClickListener {
            onLanguageClick?.invoke(language)
        }
    }

    // Возвращает количество элементов в списке
    override fun getItemCount(): Int {
        return languages.size
    }
}
