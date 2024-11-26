package com.example.newswave.data.network.api

import com.example.newswave.data.network.model.NewsResponseDto
import com.example.newswave.data.network.model.TopNewsResponseDto
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {


    @GET("top-news")
    suspend fun getListTopNews(
        @Query("source-country") sourceCountry: String = QUERY_PARAM_SOURCE_COUNTRY,
        @Query("language") language: String = QUERY_PARAM_LANGUAGE,
        @Query("date") date: String,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY
    ): Flow<TopNewsResponseDto>

    @GET("search-news")
    suspend fun getNewsByText(
        @Query("language") language: String = QUERY_PARAM_LANGUAGE,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY,
        @Query("text") text: String,
        @Query("sort-direction") sortDirection: String = QUERY_PARAM_SORT_DIRECTION,
        @Query("source-country") sourceCountry: String = QUERY_PARAM_SOURCE_COUNTRY,
        @Query("sort") sort: String = QUERY_PARAM_SORT,
        @Query("number") number: Int = QUERY_PARAM_LIMIT
    ): Flow<NewsResponseDto>

    @GET("search-news")
    suspend fun getNewsByAuthor(
        @Query("language") language: String = QUERY_PARAM_LANGUAGE,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY,
        @Query("authors") author: String,
        @Query("sort-direction") sortDirection: String = QUERY_PARAM_SORT_DIRECTION,
        @Query("source-country") sourceCountry: String = QUERY_PARAM_SOURCE_COUNTRY,
        @Query("sort") sort: String = QUERY_PARAM_SORT,
        @Query("number") limit: Int = QUERY_PARAM_LIMIT
    ): Flow<NewsResponseDto>

    @GET("top-news")
    suspend fun getNewsByDate(
        @Query("language") language: String = QUERY_PARAM_LANGUAGE,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY,
        @Query("source-country") sourceCountry: String = QUERY_PARAM_SOURCE_COUNTRY,
        @Query("date") date: String,
        ): Flow<TopNewsResponseDto>


    companion object {
        private const val QUERY_PARAM_API_KEY = "dd1c0076447947fc921616b08d0af87d"
        private const val QUERY_PARAM_LANGUAGE = "en"
        private const val QUERY_PARAM_SOURCE_COUNTRY = "us"
        private const val QUERY_PARAM_SORT_DIRECTION = "DESC"
        private const val QUERY_PARAM_SORT = "publish-time"
        private const val QUERY_PARAM_LIMIT = 50
    }
}
