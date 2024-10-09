package com.example.newswave.domain.repository

import com.example.newswave.domain.entity.NewsItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface NewsRepository{


    suspend fun getTopNewsList(): StateFlow<List<NewsItemEntity>>

    fun loadData()

    suspend fun loadNewsForPreviousDay()

    suspend fun searchNewsByFilter(filterParameter: String, valueParameter: String): List<NewsItemEntity>

}