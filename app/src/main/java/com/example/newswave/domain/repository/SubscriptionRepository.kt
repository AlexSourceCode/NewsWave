package com.example.newswave.domain.repository

import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.model.NewsState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SubscriptionRepository {

    suspend fun getAuthorList(): SharedFlow<List<AuthorItemEntity>?>

    suspend fun subscribeToAuthor(author: String)

    suspend fun unsubscribeFromAuthor(author: String)

    suspend fun favoriteAuthorCheck(author: String)

    fun isFavoriteAuthor(): StateFlow<Boolean?>

    suspend fun loadAuthorNews(author: String): SharedFlow<NewsState>
}