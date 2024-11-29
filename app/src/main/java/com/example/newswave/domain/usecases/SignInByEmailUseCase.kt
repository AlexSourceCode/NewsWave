package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SignInByEmailUseCase @Inject constructor(val repository: UserRepository) {

    operator fun invoke(email: String, password: String) = repository.signInByEmail(email, password)
}