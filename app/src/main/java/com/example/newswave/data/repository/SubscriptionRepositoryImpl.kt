package com.example.newswave.data.repository

import android.app.Application
import com.example.newswave.data.database.dbAuthors.AuthorDao
import com.example.newswave.data.database.dbAuthors.AuthorDbModel
import com.example.newswave.data.mapper.AuthorMapper
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.repository.SubscriptionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val authorDao: AuthorDao,
    private val mapper: AuthorMapper
): SubscriptionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAuthorList(): Flow<List<AuthorItemEntity>> {
        return authorDao.getAuthorsList()
            .flatMapConcat { authorsList ->
                flow {
                    emit(authorsList.map { mapper.mapDbModelToAuthorEntity(it) })
                }
            }
    }

    override suspend fun subscribeOnAuthor(author: AuthorDbModel) {
        authorDao.insertAuthor(author)
    }

    override suspend fun unsubscribeOnAuthor(author: String) {
        authorDao.deleteAuthor(author)
    }

    override suspend fun favoriteAuthorCheck(author: String): Boolean = authorDao.isAuthorExists(author)

}