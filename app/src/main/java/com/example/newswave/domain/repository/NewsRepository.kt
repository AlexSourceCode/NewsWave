package com.example.newswave.domain.repository

import com.example.newswave.domain.NewsItemEntity
import kotlinx.coroutines.flow.Flow

interface NewsRepository{

    fun getNewsDetailsById(id: Int): Flow<NewsItemEntity>

    fun getTopNewsList(): Flow<List<NewsItemEntity>>

    suspend fun loadData()

    suspend fun searchNewsByFilter(filterParameter: String, valueParameter: String)
    suspend fun getSavedNewsBySearch(): List<NewsItemEntity>

}