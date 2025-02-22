package com.example.newswave.domain.usecases.user

import com.example.newswave.domain.repositories.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(private val repository: UserRepository) {

    operator fun invoke(): StateFlow<FirebaseUser?> {
        return repository.observeAuthState()
    }
}