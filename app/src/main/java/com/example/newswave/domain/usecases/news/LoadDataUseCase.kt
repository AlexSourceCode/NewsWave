package com.example.newswave.domain.usecases.news

import com.example.newswave.domain.repositories.NewsRepository
import javax.inject.Inject

class LoadDataUseCase @Inject constructor(private val repository: NewsRepository) {

    suspend operator fun invoke() = repository.loadData()
}