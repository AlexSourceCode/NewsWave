package com.example.newswave.data.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NewsResponseDto(
    @SerializedName("top_news")
    @Expose
    val news: List<NewsTopDto>
)