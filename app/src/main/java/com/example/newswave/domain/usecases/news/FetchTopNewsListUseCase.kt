package com.example.newswave.domain.usecases.news

import com.example.newswave.domain.entities.NewsItemEntity
import com.example.newswave.domain.repositories.NewsRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class FetchTopNewsListUseCase @Inject constructor(private val repository: NewsRepository) {

    suspend operator fun invoke(): StateFlow<List<NewsItemEntity>> = repository.fetchTopNewsList()
}