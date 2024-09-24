package com.example.newswave.domain.usecases

import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.repository.SubscriptionRepository
import javax.inject.Inject

class LoadAuthorNewsUseCase @Inject constructor(private val repository: SubscriptionRepository) {

    suspend operator fun invoke(author: String):List<NewsItemEntity> = repository.loadAuthorNews(author)
}