package com.example.newswave.domain.usecases.user

import com.example.newswave.domain.repository.UserRepository
import javax.inject.Inject

class SaveContentLanguageUseCase @Inject constructor(private val repository: UserRepository) {

    suspend operator fun invoke(language: String) = repository.saveContentLanguage(language)
}