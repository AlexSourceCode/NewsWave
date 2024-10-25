package com.example.newswave.domain.repository

import com.example.newswave.data.database.dbAuthors.AuthorDbModel
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.model.NewsState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SubscriptionRepository {

    suspend fun getAuthorList(): StateFlow<List<AuthorItemEntity>>

    suspend fun subscribeToAuthor(author: String)

    suspend fun unsubscribeFromAuthor(author: String)

    suspend fun favoriteAuthorCheck(author: String): Boolean

    suspend fun loadAuthorNews(author: String): SharedFlow<NewsState>
}