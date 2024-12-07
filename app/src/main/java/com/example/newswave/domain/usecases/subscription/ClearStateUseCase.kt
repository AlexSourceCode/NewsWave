package com.example.newswave.domain.usecases.subscription

import com.example.newswave.domain.repository.SubscriptionRepository
import javax.inject.Inject

class ClearStateUseCase @Inject constructor(private val repository: SubscriptionRepository) {

    operator fun invoke() = repository.clearState()
}