package com.example.newswave.data.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NewsByAuthor (
    @SerializedName("news")
    @Expose
    val news: List<NewsItemDto>
)