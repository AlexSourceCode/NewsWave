package com.example.newswave.data.repository

import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.network.model.NewsItemDto
import com.example.newswave.data.network.model.NewsResponseDto
import com.example.newswave.data.network.model.TopNewsResponseDto
import com.example.newswave.domain.repository.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemoteDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val mapper: NewsMapper
) : RemoteDataSource {

    override suspend fun fetchTopNews(
        date: String,
        sourceCountry: String,
        language: String
    ): Flow<TopNewsResponseDto> {
        return apiService.getListTopNews(
            sourceCountry = sourceCountry,
            language = language,
            date = date,
        )
    }

    override suspend fun fetchNewsByText(
        sourceCountry: String,
        language: String,
        text: String,
        ): Flow<List<NewsItemDto>> {
        return apiService.getNewsByText(
            language = language,
            sourceCountry = sourceCountry,
            text = text,
            ).map { it.news }
    }

    override suspend fun fetchNewsByAuthor(
        author: String,
    ): Flow<List<NewsItemDto>> {
        return apiService.getNewsByAuthor(
            author = author,
        ).map { it.news }
    }

    override suspend fun fetchNewsByDate(
        date: String,
        sourceCountry: String,
        language: String
    ): Flow<List<NewsItemDto>> {
        return apiService.getNewsByDate(
            language = language,
            sourceCountry = sourceCountry,
            date = date
        ).map { mapper.mapJsonContainerTopNewsToListNews(flow { emit(it) }) }
    }

}
