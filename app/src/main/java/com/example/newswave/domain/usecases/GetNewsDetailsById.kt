package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.NewsRepository

class GetNewsDetailsById(private val repository: NewsRepository) {

    operator fun invoke(id: Int) = repository.getNewsDetailsById(id)
}