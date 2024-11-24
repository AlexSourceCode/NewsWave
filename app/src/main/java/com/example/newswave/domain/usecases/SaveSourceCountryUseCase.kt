package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.UserRepository
import javax.inject.Inject

class SaveSourceCountryUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke(country: String) = repository.saveSourceCountry(country)
}