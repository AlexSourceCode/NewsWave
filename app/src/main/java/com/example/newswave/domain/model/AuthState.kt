package com.example.newswave.domain.model

import com.google.firebase.auth.FirebaseUser

/**
 * Представляет состояние авторизации пользователя
 */
sealed class AuthState {
    class LoggedIn(val user: FirebaseUser?) : AuthState()
    data object LoggedOut: AuthState()
}