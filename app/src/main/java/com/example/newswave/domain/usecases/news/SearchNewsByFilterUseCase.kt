package com.example.newswave.domain.usecases.news

import com.example.newswave.domain.repositories.NewsRepository
import javax.inject.Inject

class SearchNewsByFilterUseCase @Inject constructor(
    private val filterParameter: String,
    private val valueParameter: String,
    private val repository: NewsRepository
) {

    suspend operator fun invoke()  = repository.searchNewsByFilter(filterParameter, valueParameter)


}