package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.SubscriptionRepository
import javax.inject.Inject

class IsFavoriteAuthorUseCase @Inject constructor(private val repository: SubscriptionRepository) {

    operator fun invoke() = repository.isFavoriteAuthor()
}