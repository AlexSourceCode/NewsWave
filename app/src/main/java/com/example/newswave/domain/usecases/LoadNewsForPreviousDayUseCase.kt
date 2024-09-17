package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.NewsRepository
import javax.inject.Inject

class LoadNewsForPreviousDayUseCase @Inject constructor(private val repository: NewsRepository) {

    suspend operator fun invoke() = repository.loadNewsForPreviousDay()
}