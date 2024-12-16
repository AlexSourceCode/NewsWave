package com.example.newswave.domain.usecases.news

import com.example.newswave.domain.repositories.NewsRepository
import javax.inject.Inject

class ClearTopNewsRepositoryUseCase @Inject constructor(private val repository: NewsRepository) {

    operator fun invoke() = repository.clear()
}