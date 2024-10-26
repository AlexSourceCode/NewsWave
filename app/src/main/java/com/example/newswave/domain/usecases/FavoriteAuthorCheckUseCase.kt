package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.SubscriptionRepository
import javax.inject.Inject

class FavoriteAuthorCheckUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {

    suspend operator fun invoke(author: String) {
        repository.favoriteAuthorCheck(author)
    }
}