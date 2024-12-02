package com.example.newswave.data.repository

import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.database.dbNews.NewsDbModel
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.repository.LocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class LocalDataSourceImpl @Inject constructor(
    private val newsDao: NewsDao,
    private val mapper: NewsMapper
): LocalDataSource {

    private val ioScope = CoroutineScope(Dispatchers.IO)


    override fun getNewsList(): StateFlow<List<NewsItemEntity>> =
        newsDao.getNewsList().map { newsList ->
            newsList.map {
                newsEntities -> mapper.mapDbModelToEntity(newsEntities)
            }
        }.stateIn(
            scope = ioScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList(),
        )

    override suspend fun insertNews(newsList: List<NewsDbModel>) {
        newsDao.insertNews(newsList)
    }

    override suspend fun deleteAllNews() {
        newsDao.deleteAllNews()
    }

}