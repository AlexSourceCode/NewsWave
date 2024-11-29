package com.example.newswave.domain.repository

import com.example.newswave.data.network.model.NewsItemDto
import com.example.newswave.data.network.model.NewsResponseDto
import com.example.newswave.data.network.model.TopNewsResponseDto
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    suspend fun fetchTopNews(
        date: String,
        sourceCountry: String,
        language: String
    ): Flow<TopNewsResponseDto>

    suspend fun fetchNewsByText(
        sourceCountry: String,
        language: String,
        text: String,
    ): Flow<List<NewsItemDto>>

    suspend fun fetchNewsByAuthor(
        author: String,
    ): Flow<List<NewsItemDto>>

    suspend fun fetchNewsByDate(
        date: String,
        sourceCountry: String,
        language: String
    ): Flow<List<NewsItemDto>>

}
