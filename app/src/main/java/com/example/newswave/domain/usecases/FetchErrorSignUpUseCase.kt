package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class FetchErrorSignUpUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke(): SharedFlow<String> = repository.fetchErrorSignUp()
}