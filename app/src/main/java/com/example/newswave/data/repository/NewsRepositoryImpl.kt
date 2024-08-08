package com.example.newswave.data.repository

import android.app.Application
import android.util.Log
import com.example.newswave.data.database.dbNews.NewsDb
import com.example.newswave.data.database.dbNews.NewsDbModel
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.mapper.flattenToList
import com.example.newswave.data.network.api.ApiFactory
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.network.model.NewsResponseDto
import com.example.newswave.domain.NewsInfo
import com.example.newswave.domain.repository.NewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

class NewsRepositoryImpl(
    application: Application,
): NewsRepository {

    private val newsInfoDao = NewsDb.getInstance(application)
    private val mapper = NewsMapper()
    private val apiService = ApiFactory.apiService

    override fun getNewsDetailsById(id: Int): Flow<NewsInfo> {
        return newsInfoDao.newsDao().getNewsDetailsById(id)
            .map { mapper.dbModelToEntity(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTopNewsList(): Flow<List<NewsInfo>> {
        return newsInfoDao.newsDao().getNewsList()
            .flatMapConcat { newsList ->
                flow {
                    emit(newsList.map { mapper.dbModelToEntity(it) })
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun loadData() {

            try {
                val jsonContainer: Flow<NewsResponseDto> = apiService.getListTopNews()

                val newsList: Flow<List<NewsDbModel>> = jsonContainer //преобразование из Flow<NewsResponseDto> в Flow<List<NewsItemDto>>
                    .map { mapper.mapJsonContainerToListNews(jsonContainer) }
                    .flatMapConcat { newList ->
                        flow {
                            emit(newList.map { mapper.mapDtoToDbModel(it) })
                        }
                    }






                val flatNewsList: List<NewsDbModel> = newsList.flattenToList() //преобразование в List<NewsDbModel>
                newsInfoDao.newsDao().insertNews(flatNewsList)
            } catch (e: Exception) {
                Log.e("CheckNews", "Error fetching top news", e)
            }
            delay(10000)

    }




}