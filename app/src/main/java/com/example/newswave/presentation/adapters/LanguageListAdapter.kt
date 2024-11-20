package com.example.newswave.presentation.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.newswave.R
import com.example.newswave.databinding.ListItemBinding
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.presentation.adapters.AuthorListDiffCallback
import com.example.newswave.presentation.adapters.AuthorListViewHolder
import com.example.newswave.presentation.adapters.LanguageListDiffCallBack

class LanguageListAdapter(private val languages: List<String>) :
    ListAdapter<String, LanguageListAdapter.ViewHolder>(
        LanguageListDiffCallBack
    ) {


    var currentLanguageChecked: ((String) -> Boolean)? = null
    var onLanguageClick: ((String) -> Unit)? = null

    class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val language = languages[position]
        holder.binding.textItem.text = languages[position]

        if (currentLanguageChecked?.invoke(language) == true) {
            holder.binding.checkedImage.visibility = View.VISIBLE
        }

        holder.binding.textItem.setOnClickListener {
            onLanguageClick?.invoke(language)
        }
    }

    override fun getItemCount(): Int {
        return languages.size
    }
}
