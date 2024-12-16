package com.example.newswave.domain.usecases.user

import com.example.newswave.domain.repositories.UserRepository
import javax.inject.Inject

class IsUserDataUpdatedUseCase @Inject constructor(private val repository: UserRepository) {


    operator fun invoke() = repository.isUserDataUpdated()
}