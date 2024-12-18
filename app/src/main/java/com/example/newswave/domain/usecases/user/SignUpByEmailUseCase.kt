package com.example.newswave.domain.usecases.user

import com.example.newswave.domain.repositories.UserRepository
import javax.inject.Inject

class SignUpByEmailUseCase @Inject constructor(val repository: UserRepository) {

    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) = repository.signUpByEmail(
        username,
        email,
        password,
        firstName,
        lastName
    )
}