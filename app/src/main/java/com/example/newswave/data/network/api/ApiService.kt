package com.example.newswave.data.network.api

import com.example.newswave.data.network.model.NewsResponseDto
import com.example.newswave.data.network.model.TopNewsResponseDto
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Интерфейс для взаимодействия с API новостей
 */
interface ApiService {

    // Получение списка топ-новостей
    @GET("top-news")
    suspend fun getListTopNews(
        @Query("source-country") sourceCountry: String,
        @Query("language") language: String,
        @Query("date") date: String,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY
    ): Flow<TopNewsResponseDto>

    // Поиск новостей по тексту
    @GET("search-news")
    suspend fun getNewsByText(
        @Query("language") language: String,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY,
        @Query("text") text: String,
        @Query("sort-direction") sortDirection: String = QUERY_PARAM_SORT_DIRECTION,
        @Query("source-country") sourceCountry: String,
        @Query("sort") sort: String = QUERY_PARAM_SORT,
        @Query("number") number: Int = QUERY_PARAM_LIMIT
    ): Flow<NewsResponseDto>

    // Поиск новостей по автору
    @GET("search-news")
    suspend fun getNewsByAuthor(
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY,
        @Query("authors") author: String,
        @Query("sort-direction") sortDirection: String = QUERY_PARAM_SORT_DIRECTION,
        @Query("sort") sort: String = QUERY_PARAM_SORT,
        @Query("number") limit: Int = QUERY_PARAM_LIMIT
    ): Flow<NewsResponseDto>

    // Поиск новостей по дате
    @GET("top-news")
    suspend fun getNewsByDate(
        @Query("language") language: String,
        @Query("api-key") apiKey: String = QUERY_PARAM_API_KEY,
        @Query("source-country") sourceCountry: String,
        @Query("date") date: String,
        ): Flow<TopNewsResponseDto>


    companion object {
        private const val QUERY_PARAM_API_KEY = "b6ef41d59ba74add937bb849af4656b4"
        private const val QUERY_PARAM_SORT_DIRECTION = "DESC"
        private const val QUERY_PARAM_SORT = "publish-time"
        private const val QUERY_PARAM_LIMIT = 50
    }
}
