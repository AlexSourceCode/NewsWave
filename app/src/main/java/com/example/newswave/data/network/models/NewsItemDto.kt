package com.example.newswave.data.network.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


/**
 * DTO для описания одной новости
 */
data class NewsItemDto(
    @Expose
    @SerializedName("id")
    val id: Int,

    @Expose
    @SerializedName("title")
    val title: String,

    @Expose
    @SerializedName("text")
    val text: String,

    @Expose
    @SerializedName("url")
    val url: String,

    @Expose
    @SerializedName("image")
    val image: String?,

    @Expose
    @SerializedName("video")
    val video: String?,

    @Expose
    @SerializedName("publish_date")
    val publishDate: String,

    @Expose
    @SerializedName("author")
    val author: String? = null,

    @Expose
    @SerializedName("language")
    val language: String,

    @Expose
    @SerializedName("source_country")
    val sourceCountry: String
)