package com.example.newswave.data.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * DTO для ответа API на запрос новостей
 */
data class NewsResponseDto (
    @SerializedName("news")
    @Expose
    val news: List<NewsItemDto>
)