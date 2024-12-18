package com.example.newswave.domain.usecases.user

import com.example.newswave.domain.repositories.UserRepository
import javax.inject.Inject

class SignInByEmailUseCase @Inject constructor(val repository: UserRepository) {

    suspend operator fun invoke(email: String, password: String) = repository.signInByEmail(email, password)
}