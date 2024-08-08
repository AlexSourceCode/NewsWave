package com.example.newswave.data.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NewsTopDto(
    @SerializedName("news")
    @Expose
    val newsTop: List<NewsItemDto>
)
