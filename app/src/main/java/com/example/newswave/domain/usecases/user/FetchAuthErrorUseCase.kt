package com.example.newswave.domain.usecases.user

import com.example.newswave.data.network.models.ErrorType
import com.example.newswave.domain.repositories.UserRepository
import javax.inject.Inject

class FetchAuthErrorUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke(type: ErrorType) = repository.fetchError(type)
}