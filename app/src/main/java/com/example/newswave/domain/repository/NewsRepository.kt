package com.example.newswave.domain.repository

import com.example.newswave.domain.entity.NewsItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface NewsRepository{


    suspend fun fetchTopNewsList(): StateFlow<List<NewsItemEntity>>

    suspend fun fetchErrorLoadData(): SharedFlow<String>

    suspend fun loadData()

    suspend fun loadNewsForPreviousDay()

    suspend fun searchNewsByFilter(filterParameter: String, valueParameter: String): SharedFlow<List<NewsItemEntity>>

}