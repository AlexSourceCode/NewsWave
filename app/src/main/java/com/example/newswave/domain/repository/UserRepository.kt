package com.example.newswave.domain.repository

import com.example.newswave.data.network.model.ErrorType
import com.example.newswave.domain.entity.UserEntity
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для управления данными пользователя
 */
interface UserRepository {

    // Сбросить пароль пользователя
    suspend fun resetPassword(email: String)

    // Войти в систему с использованием email и пароля
    suspend fun signInByEmail(email: String, password: String)

    // Зарегистрировать пользователя с использованием email
    suspend fun signUpByEmail(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    )

    // Получить сообщение об ошибке по указанному типу
    fun fetchError(type: ErrorType): SharedFlow<String>

    // Выйти из системы
    suspend fun signOut()

    // Получить текущий язык контента
    fun getContentLanguage(): String

    // Наблюдать за обновлением данных пользователя
    fun isUserDataUpdated(): SharedFlow<Unit>

    // Сохранить текущий язык контента
    suspend fun saveContentLanguage(language: String)

    // Получить текущую страну источника новостей
    fun getSourceCountry(): String

    // Сохранить страну источника новостей
    suspend fun saveSourceCountry(country: String)

    // Проверить успешность аутентификации
    fun fetchIsSuccessAuth(): SharedFlow<Boolean>

    // Наблюдать за состоянием аутентификации пользователя
    fun observeAuthState(): StateFlow<FirebaseUser?>

    // Получить данные текущего пользователя
    fun fetchUserData(): StateFlow<UserEntity?>

    // Наблюдать за языком контента
    fun fetchContentLanguage(): StateFlow<String>

    // Наблюдать за страной источника новостей
    fun fetchSourceCountry(): StateFlow<String>

    // Очистить корутину, чтобы не было утечек памяти
    fun clear()

}