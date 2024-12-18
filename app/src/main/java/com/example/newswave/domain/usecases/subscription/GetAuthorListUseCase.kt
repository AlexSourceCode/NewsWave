package com.example.newswave.domain.usecases.subscription

import com.example.newswave.domain.repositories.SubscriptionRepository
import javax.inject.Inject

class GetAuthorListUseCase @Inject constructor(private val repository: SubscriptionRepository) {

    suspend operator fun invoke() = repository.getAuthorList()
}