package com.example.newswave.domain.usecases

import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.repository.NewsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Inject

class SearchNewsByFilterUseCase @Inject constructor(
    private val filterParameter: String,
    private val valueParameter: String,
    private val repository: NewsRepository
) {

    suspend operator fun invoke(): List<NewsItemEntity> = repository.searchNewsByFilter(filterParameter, valueParameter)


}