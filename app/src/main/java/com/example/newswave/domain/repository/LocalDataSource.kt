package com.example.newswave.domain.repository

import com.example.newswave.data.database.dbNews.NewsDbModel
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {

    fun getNewsList(): Flow<List<NewsDbModel>>
    suspend fun insertNews(newsList: List<NewsDbModel>)
    suspend fun deleteAllNews()
}