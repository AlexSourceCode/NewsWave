package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.NewsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Inject

class SearchNewsByFilterUseCase(
    private val filterParameter: String,
    private val valueParameter: String,
    private val repository: NewsRepository
) {

    suspend operator fun invoke() = repository.searchNewsByFilter(filterParameter, valueParameter)


}