package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.NewsRepository
import javax.inject.Inject

class GetTopNewsList @Inject constructor(private val repository: NewsRepository) {

    operator fun invoke() = repository.getTopNewsList()
}