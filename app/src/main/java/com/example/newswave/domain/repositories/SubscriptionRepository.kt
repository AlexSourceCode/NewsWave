package com.example.newswave.domain.repositories

import com.example.newswave.domain.entities.AuthorItemEntity
import com.example.newswave.domain.entities.NewsItemEntity
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
    suspend fun favoriteAuthorCheck(author: String)

    // Узнать, является ли текущий автор избранным
    fun isFavoriteAuthor(): StateFlow<Boolean?>

    // Очистить текущее состояние подписок
    fun clearState()

    // Загрузить новости определённого автора
    suspend fun loadAuthorNews(author: String): SharedFlow<List<NewsItemEntity>>

    // Очистить корутину, чтобы не было утечек памяти
    fun clear()
}