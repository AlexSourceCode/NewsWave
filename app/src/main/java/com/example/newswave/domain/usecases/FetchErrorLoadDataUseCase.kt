package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.NewsRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class FetchErrorLoadDataUseCase @Inject constructor(private val repository: NewsRepository) {

    suspend operator fun invoke(): SharedFlow<String> = repository.fetchErrorLoadData()
}