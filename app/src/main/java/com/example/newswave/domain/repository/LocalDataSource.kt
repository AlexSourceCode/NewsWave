package com.example.newswave.domain.repository

import com.example.newswave.data.database.dbNews.NewsDbModel
import com.example.newswave.domain.entity.NewsItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LocalDataSource {

    fun getNewsList(): StateFlow<List<NewsItemEntity>>
    suspend fun insertNews(newsList: List<NewsDbModel>)
    suspend fun deleteAllNews()
}