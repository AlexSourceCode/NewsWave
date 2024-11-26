package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class FetchErrorSignInUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke(): SharedFlow<String> = repository.fetchErrorSignIn()
}