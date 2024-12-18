package com.example.newswave.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Представляет новостную статью с её метаданными
 */
@Parcelize
data class NewsItemEntity(
    val id: Int,
    val title: String,
    val text: String,
    val url: String,
    val image: String?,
    val video: String?,
    val publishDate: String,
    val author: String,
    val language: String,
    val sourceCountry: String
) : Parcelable
