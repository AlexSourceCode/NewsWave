package com.example.newswave.domain.repository

import com.example.newswave.data.dataSource.local.NewsDbModel
import com.example.newswave.data.network.model.NewsItemDto
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.utils.Filter
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    suspend fun fetchTopNews(
        date: String,
    ): List<NewsDbModel>

    suspend fun fetchNewsByText(
        text: String,
    ): Flow<List<NewsItemDto>>

    suspend fun fetchNewsByAuthor(
        author: String,
    ): Flow<List<NewsItemDto>>

    suspend fun fetchNewsByDate(
        date: String,
    ): Flow<List<NewsItemDto>>

    suspend fun fetchFilteredNews(
        filterType: Filter,
        filterValue: String
    ): Flow<List<NewsItemEntity>>

    suspend fun fetchNewsByAuthorFlow(author: String): Flow<List<NewsItemEntity>>
}
