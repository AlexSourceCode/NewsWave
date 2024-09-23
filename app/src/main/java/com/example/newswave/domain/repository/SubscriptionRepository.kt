package com.example.newswave.domain.repository

import com.example.newswave.data.database.dbAuthors.AuthorDbModel
import com.example.newswave.domain.entity.AuthorItemEntity
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {

    fun getAuthorList(): Flow<List<AuthorItemEntity>>

    suspend fun subscribeOnAuthor(author: AuthorDbModel)

    suspend fun unsubscribeOnAuthor(author: String)

    suspend fun favoriteAuthorCheck(author: String): Boolean

}