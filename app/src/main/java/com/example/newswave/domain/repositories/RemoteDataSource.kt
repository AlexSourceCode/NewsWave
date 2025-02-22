package com.example.newswave.domain.repositories

import com.example.newswave.data.source.local.NewsDbModel
import com.example.newswave.data.network.models.NewsItemDto
import com.example.newswave.domain.entities.NewsItemEntity
import com.example.newswave.domain.model.Filter
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для удаленного источника данных (API)
 */
interface RemoteDataSource {

    // Получить список топовых новостей за указанную дату
    suspend fun fetchTopNews(
        date: String,
    ): List<NewsDbModel>

    // Найти новости по текстовому запросу
    suspend fun fetchNewsByText(
        text: String,
    ): Flow<List<NewsItemDto>>

    // Найти новости по имени автора
    suspend fun fetchNewsByAuthor(
        author: String,
    ): Flow<List<NewsItemDto>>

    // Найти новости по дате
    suspend fun fetchNewsByDate(
        date: String,
    ): Flow<List<NewsItemDto>>

    // Найти новости с фильтрацией
    suspend fun fetchFilteredNews(
        filterType: Filter,
        filterValue: String
    ): Flow<List<NewsItemEntity>>

    // Найти новости по имени автора с результатами в формате домена
    suspend fun fetchNewsByAuthorFlow(author: String): Flow<List<NewsItemEntity>>
}
