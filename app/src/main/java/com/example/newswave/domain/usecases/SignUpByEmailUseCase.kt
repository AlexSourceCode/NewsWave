package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.UserRepository
import javax.inject.Inject

class SignUpByEmailUseCase @Inject constructor(val repository: UserRepository) {

    operator fun invoke(
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