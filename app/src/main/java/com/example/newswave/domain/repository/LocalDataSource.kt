package com.example.newswave.domain.repository

import com.example.newswave.data.dataSource.local.NewsDbModel
import com.example.newswave.domain.entity.NewsItemEntity
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для локального источника данных (NewsDb).
 */
interface LocalDataSource {

    // Получить список новостей из локальной базы данных в виде потока.
    fun getNewsList(): StateFlow<List<NewsItemEntity>>

    // Вставить список новостей в локальную базу данных
    suspend fun insertNews(newsList: List<NewsDbModel>)

    // Удалить все новости из локальной базы данных
    suspend fun deleteAllNews()
}