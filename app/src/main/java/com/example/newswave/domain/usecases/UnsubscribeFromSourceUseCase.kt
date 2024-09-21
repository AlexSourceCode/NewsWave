package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.SubscriptionRepository
import javax.inject.Inject

class UnsubscribeFromSourceUseCase @Inject constructor(private val repository: SubscriptionRepository) {
}