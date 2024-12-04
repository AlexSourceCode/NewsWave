package com.example.newswave.data.dataSource.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сущность базы данных для хранения новостей
  */
@Entity(tableName = "news")
data class NewsDbModel(
    @PrimaryKey
    val id: Int,
    val title: String,
    val text: String,
    val url: String,
    val image: String? = null,
    val video: String? = null,
    val publishDate: String,
    val author: String,
    val language: String,
    val sourceCountry: String
)
