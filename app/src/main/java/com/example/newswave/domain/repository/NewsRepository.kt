package com.example.newswave.domain.repository

import com.example.newswave.data.network.model.NewsItemDto
import com.example.newswave.data.network.model.NewsResponseDto
import com.example.newswave.domain.NewsInfo
import kotlinx.coroutines.flow.Flow

interface NewsRepository{

    fun getNewsDetailsById(id: Int): Flow<NewsInfo>

    fun getTopNewsList(): Flow<List<NewsInfo>>

    suspend fun loadData()


}