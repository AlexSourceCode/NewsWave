package com.example.newswave.domain.repository

import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.model.NewsState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для управления подписками на авторов.
 */
interface SubscriptionRepository {

    // Получить список авторов
    suspend fun getAuthorList(): SharedFlow<List<AuthorItemEntity>?>

    // Подписаться на автора.
    suspend fun subscribeToAuthor(author: String)

    // Отписаться от автора
    suspend fun unsubscribeFromAuthor(author: String)

    // Проверить, является ли автор избранным
    fun favoriteAuthorCheck(author: String)

    // Узнать, является ли текущий автор избранным
    fun isFavoriteAuthor(): StateFlow<Boolean?>

    // Показать список авторов
    fun showAuthorsList()

    // Очистить текущее состояние подписок
    fun clearState()

    // Загрузить новости определённого автора
    suspend fun loadAuthorNews(author: String): SharedFlow<NewsState>
}