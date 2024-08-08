package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.NewsRepository

class GetTopNewsList(private val repository: NewsRepository) {

    operator fun invoke() = repository.getTopNewsList()
}