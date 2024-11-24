package com.example.newswave.domain.usecases

import com.example.newswave.domain.repository.UserRepository
import javax.inject.Inject

class SyncUserSettingsUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke() = repository.syncUserSettings()
}