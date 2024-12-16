package com.example.newswave.domain.usecases.user

import com.example.newswave.domain.repositories.UserRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetContentLanguageUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke(): StateFlow<String> = repository.fetchContentLanguage()
}