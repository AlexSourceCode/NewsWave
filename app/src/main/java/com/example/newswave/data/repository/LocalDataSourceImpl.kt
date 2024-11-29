package com.example.newswave.data.repository

import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.database.dbNews.NewsDbModel
import com.example.newswave.domain.repository.LocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSourceImpl @Inject constructor(
    private val newsDao: NewsDao
): LocalDataSource {
    override fun getNewsList(): Flow<List<NewsDbModel>> = newsDao.getNewsList()

    override suspend fun insertNews(newsList: List<NewsDbModel>) {
        newsDao.insertNews(newsList)
    }

    override suspend fun deleteAllNews() {
        newsDao.deleteAllNews()
    }

}