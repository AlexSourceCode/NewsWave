package com.example.newswave.data.network.api

import android.annotation.SuppressLint
import com.example.newswave.data.network.model.NewsByAuthor
import com.example.newswave.data.network.model.NewsItemDto
import com.example.newswave.data.network.model.NewsResponseDto
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface ApiService {


    @GET("top-news")
    suspend fun getListTopNews(
        @Query("source-country") sourceCountry: String = QUERY_PARAM_SOURCE_COUNTRY,
        @Query("language") language: String = QUERY_PARAM_LANGUAGE,
        @Query("date") date: String,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY
    ): Flow<NewsResponseDto>

    @GET("retrieve-news")
    suspend fun getNewsById(
        newsId: Int,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY,
        @Query("ids") id: Int = newsId
    ): Flow<NewsItemDto>

    @GET("search-news")
    suspend fun getNewsByAuthor(
        authorNews: String,
        @Query("language") language: String = QUERY_PARAM_LANGUAGE,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY,
        @Query("authors") author: String = authorNews
    ): Flow<NewsByAuthor>

    companion object {
        private const val QUERY_PARAM_API_KEY = "b6ef41d59ba74add937bb849af4656b4"
        private const val QUERY_PARAM_LANGUAGE = "en"
        private const val QUERY_PARAM_SOURCE_COUNTRY = "us"



    }
}