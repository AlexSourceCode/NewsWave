package com.example.newswave.domain.repository

import com.example.newswave.data.dataSource.local.NewsDbModel
import com.example.newswave.domain.entity.NewsItemEntity
import kotlinx.coroutines.flow.StateFlow

interface LocalDataSource {

    fun getNewsList(): StateFlow<List<NewsItemEntity>>
    suspend fun insertNews(newsList: List<NewsDbModel>)
    suspend fun deleteAllNews()
}