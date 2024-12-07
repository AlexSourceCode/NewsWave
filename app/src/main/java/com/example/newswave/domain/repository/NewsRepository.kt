package com.example.newswave.domain.repository

import com.example.newswave.domain.entity.NewsItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для управления новостными данными.
 */
interface NewsRepository{

    // Получить список популярных новостей
    suspend fun fetchTopNewsList(): StateFlow<List<NewsItemEntity>>

    // Получить ошибки загрузки данных
    suspend fun fetchErrorLoadData(): SharedFlow<String>

    // Загрузить данные в приложение
    suspend fun loadData()

    // Загрузить новости за предыдущий день
    suspend fun loadNewsForPreviousDay()

    // Выполнить поиск новостей по фильтру
    suspend fun searchNewsByFilter(filterParameter: String, valueParameter: String): SharedFlow<List<NewsItemEntity>>

}