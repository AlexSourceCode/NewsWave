package com.example.newswave.domain.usecases

import com.example.newswave.data.network.model.ErrorType
import com.example.newswave.domain.repository.UserRepository
import javax.inject.Inject

class FetchAuthErrorUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke(type: ErrorType) = repository.fetchError(type)
}