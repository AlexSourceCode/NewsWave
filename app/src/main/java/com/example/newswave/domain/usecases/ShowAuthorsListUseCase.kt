package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.SubscriptionRepository
import javax.inject.Inject

class ShowAuthorsListUseCase @Inject constructor(private val repository: SubscriptionRepository) {

    operator fun invoke(){
        repository.showAuthorsList()
    }
}