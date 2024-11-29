package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.UserRepository
import javax.inject.Inject

class SaveContentLanguageUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke(language: String) = repository.saveContentLanguage(language)
}