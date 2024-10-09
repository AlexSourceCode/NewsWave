package com.example.newswave.domain.usecases

import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.repository.NewsRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetTopNewsListUseCase @Inject constructor(private val repository: NewsRepository) {

    suspend operator fun invoke(): StateFlow<List<NewsItemEntity>> {
        return repository.getTopNewsList()
    }
}