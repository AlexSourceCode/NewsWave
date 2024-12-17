package com.example.newswave.domain.repositories

import com.example.newswave.domain.entities.NewsItemEntity
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

    suspend fun getInterfaceLanguage(): String

    fun clear()
}