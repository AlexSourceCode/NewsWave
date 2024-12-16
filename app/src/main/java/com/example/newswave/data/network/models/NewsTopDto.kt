package com.example.newswave.data.network.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * DTO для ответа API на запрос популярных новостей.
 */
data class NewsTopDto(
    @SerializedName("news")
    @Expose
    val newsTop: List<NewsItemDto>
)
