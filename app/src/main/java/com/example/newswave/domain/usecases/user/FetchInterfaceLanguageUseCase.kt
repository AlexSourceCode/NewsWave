package com.example.newswave.domain.usecases.user

import com.example.newswave.domain.repositories.UserRepository
import javax.inject.Inject

class FetchInterfaceLanguageUseCase @Inject constructor(private val repository: UserRepository) {

    suspend operator fun invoke() = repository.fetchInterfaceLanguage()
}