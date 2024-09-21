package com.example.newswave.data.repository

import android.app.Application
import com.example.newswave.data.database.dbAuthors.AuthorDao
import com.example.newswave.data.database.dbAuthors.AuthorDbModel
import com.example.newswave.domain.repository.SubscriptionRepository
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val authorDao: AuthorDao
): SubscriptionRepository {
    override suspend fun subscribeOnAuthor(author: AuthorDbModel) {
        authorDao.insertAuthor(author)
    }

    override suspend fun unsubscribeOnAuthor(author: String) {
        authorDao.deleteAuthor(author)
    }
}