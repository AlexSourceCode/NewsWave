package com.example.newswave.domain.repository

import com.example.newswave.data.database.dbAuthors.AuthorDbModel

interface SubscriptionRepository {

    suspend fun subscribeOnAuthor(author: AuthorDbModel)

    suspend fun unsubscribeOnAuthor(author: String)

}