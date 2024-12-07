package com.example.newswave.domain.usecases.user

import com.example.newswave.domain.repository.UserRepository
import javax.inject.Inject

class SignInByEmailUseCase @Inject constructor(val repository: UserRepository) {

    operator fun invoke(email: String, password: String) = repository.signInByEmail(email, password)
}