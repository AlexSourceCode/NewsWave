package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.NewsRepository
import javax.inject.Inject

class SearchNewsByFilterUseCase @Inject constructor(
    private val filterParameter: String,
    private val valueParameter: String,
    private val repository: NewsRepository
) {

    suspend operator fun invoke() = repository.searchNewsByFilter(filterParameter,valueParameter)
}