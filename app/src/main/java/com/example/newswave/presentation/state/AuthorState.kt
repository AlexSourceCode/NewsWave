package com.example.newswave.presentation.state

import com.example.newswave.domain.entity.AuthorItemEntity

/**
 * Представляет состояние пользовательского интерфейса для данных об авторах
 */
sealed class AuthorState {
    data class Error(val message: String, val timestamp: Long = System.currentTimeMillis()) :
        AuthorState()

    object Loading : AuthorState()
    data class Success(val currentList: List<AuthorItemEntity>?) : AuthorState()
}
