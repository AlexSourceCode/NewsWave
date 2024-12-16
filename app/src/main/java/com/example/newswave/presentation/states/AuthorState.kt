package com.example.newswave.presentation.states

import com.example.newswave.domain.entities.AuthorItemEntity

/**
 * Представляет состояние пользовательского интерфейса для данных об авторах
 */
sealed class AuthorState {
    data class Error(val message: String, val timestamp: Long = System.currentTimeMillis()) :
        AuthorState()

    object Loading : AuthorState()
    data class Success(val currentList: List<AuthorItemEntity>?) : AuthorState()
}
