package com.example.newswave.domain.usecases.subscription

import com.example.newswave.domain.repositories.SubscriptionRepository
import javax.inject.Inject

class ClearSubscriptionRepositoryUseCase @Inject constructor(private val repository: SubscriptionRepository) {

    operator fun invoke() = repository.clear()
}