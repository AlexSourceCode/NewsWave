package com.example.newswave.data.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * DTO для ответа API на запрос топ-новостей.
 */
data class TopNewsResponseDto(
    @SerializedName("top_news")
    @Expose
    val news: List<NewsTopDto>
)