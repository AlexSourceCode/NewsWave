package com.example.newswave.domain


data class NewsInfo(
    val id: Int,
    val title: String,
    val text: String,
    val url: String,
    val image: String?,
    val video: String?,
    val publishDate: String,
    val author: String,
    val language: String,
    val category: String? = null,
    val sourceCountry: String
)
