package com.example.newswave.domain.usecases

import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.repository.NewsRepository

class GetSavedNewsBySearchUseCase(private val repository: NewsRepository) {

    suspend operator fun invoke(): List<NewsItemEntity> {
        return repository.getSavedNewsBySearch()
    }
}