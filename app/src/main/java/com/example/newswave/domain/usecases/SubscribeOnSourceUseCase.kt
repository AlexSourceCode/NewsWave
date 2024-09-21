package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.SubscriptionRepository
import javax.inject.Inject

class SubscribeOnSourceUseCase @Inject constructor(private val repository: SubscriptionRepository) {
}