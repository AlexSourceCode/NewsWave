package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.NewsRepository

class LoadNewsForPreviousDayUseCase(private val repository: NewsRepository) {

    suspend operator fun invoke() = repository.loadNewsForPreviousDay()
}