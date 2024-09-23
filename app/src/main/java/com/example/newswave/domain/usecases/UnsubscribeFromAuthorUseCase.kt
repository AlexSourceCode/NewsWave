package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.SubscriptionRepository
import javax.inject.Inject

class UnsubscribeFromAuthorUseCase @Inject constructor(private val repository: SubscriptionRepository) {

    suspend operator fun invoke(author: String) = repository.unsubscribeOnAuthor(author)
}