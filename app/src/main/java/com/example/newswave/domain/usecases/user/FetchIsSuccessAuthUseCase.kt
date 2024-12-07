package com.example.newswave.domain.usecases.user

import com.example.newswave.domain.repository.UserRepository
import javax.inject.Inject

class FetchIsSuccessAuthUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke() = repository.fetchIsSuccessAuth()
}