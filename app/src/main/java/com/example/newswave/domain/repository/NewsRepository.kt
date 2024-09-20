package com.example.newswave.domain.repository

import com.example.newswave.domain.entity.NewsItemEntity
import kotlinx.coroutines.flow.Flow

interface NewsRepository{

    fun getNewsDetailsById(id: Int): Flow<NewsItemEntity>

    fun getTopNewsList(): Flow<List<NewsItemEntity>>

    fun loadData()
    suspend fun loadNewsForPreviousDay()

    suspend fun searchNewsByFilter(filterParameter: String, valueParameter: String)
    suspend fun getSavedNewsBySearch(): List<NewsItemEntity>

}