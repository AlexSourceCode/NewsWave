package com.example.newswave.domain.usecases.news

import com.example.newswave.domain.repositories.NewsRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class FetchErrorLoadDataUseCase @Inject constructor(private val repository: NewsRepository) {

    suspend operator fun invoke(): SharedFlow<String> = repository.fetchErrorLoadData()
}