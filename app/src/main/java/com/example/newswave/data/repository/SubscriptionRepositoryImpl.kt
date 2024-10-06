package com.example.newswave.data.repository

import android.app.Application
import android.content.Context
import com.example.newswave.data.database.dbAuthors.AuthorDao
import com.example.newswave.data.database.dbAuthors.AuthorDbModel
import com.example.newswave.data.mapper.AuthorMapper
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.mapper.flattenToList
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.repository.SubscriptionRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val authorDao: AuthorDao,
    private val mapper: AuthorMapper,
    private val mapperNews: NewsMapper,
    private val apiService: ApiService
) : SubscriptionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getAuthorList(): Flow<List<AuthorItemEntity>> =
        withContext(Dispatchers.IO){
            authorDao.getAuthorsList()
            .flatMapConcat { authorsList ->
                flow {
                    emit(authorsList.map { mapper.mapDbModelToAuthorEntity(it) })
                }
            }
    }

    override suspend fun loadAuthorNews(author: String): List<NewsItemEntity> =
        withContext(Dispatchers.IO) {
            apiService.getNewsByAuthor(author = author)
                .map { it.news }
                .flatMapConcat { newsList ->
                    flow { emit(newsList.map { mapperNews.mapDtoToEntity(it) }) }
                }
                .flattenToList()
                .distinctBy { it.title }
        }


    override suspend fun subscribeOnAuthor(author: String) {
        val author = AuthorDbModel(
            author = author
        )
        authorDao.insertAuthor(author)
    }

    override suspend fun unsubscribeOnAuthor(author: String) {
        authorDao.deleteAuthor(author)
    }

    override suspend fun favoriteAuthorCheck(author: String): Boolean =
        authorDao.isAuthorExists(author)

}