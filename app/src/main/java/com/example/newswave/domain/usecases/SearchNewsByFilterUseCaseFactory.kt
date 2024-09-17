package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.NewsRepository
import javax.inject.Inject

class SearchNewsByFilterUseCaseFactory @Inject constructor(
    private val repository: NewsRepository
) {
    fun create(filterParameter: String, valueParameter: String): SearchNewsByFilterUseCase {
        return SearchNewsByFilterUseCase(filterParameter, valueParameter, repository)
    }
}