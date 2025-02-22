package com.example.newswave.data.repositories

import com.example.newswave.data.source.local.NewsDao
import com.example.newswave.data.source.local.NewsDbModel
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.domain.entities.NewsItemEntity
import com.example.newswave.domain.repositories.LocalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Реализация интерфейса LocalDataSource, обеспечивающая работу с локальной базой данных новостей
 * Использует NewsDao для выполнения операций с БД и NewsMapper для преобразования данных
 */
class LocalDataSourceImpl @Inject constructor(
    private val newsDao: NewsDao,
    private val mapper: NewsMapper
) : LocalDataSource {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    // Возвращает список новостей из локальной базы данных в формате StateFlow
    // Данные преобразуются из NewsDbModel в NewsItemEntity
    override fun getNewsList(): StateFlow<List<NewsItemEntity>> {
        return newsDao.getNewsList().map { newsList ->
            newsList.map { newsEntities ->
                mapper.mapDbModelToEntity(newsEntities)
            }
        }.stateIn(
            scope = ioScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList(),
        )
    }


    // Сохраняет список новостей в локальную базу данных
    override suspend fun insertNews(newsList: List<NewsDbModel>) {
        newsDao.insertNews(newsList)
    }

    // Удаляет все записи из локальной базы данных
    override suspend fun deleteAllNews() {
        newsDao.deleteAllNews()
    }
}