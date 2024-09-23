package com.example.newswave.data.repository

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.mapper.flattenToList
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.network.model.TopNewsResponseDto
import com.example.newswave.data.workers.RefreshDataWorker
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.repository.NewsRepository
import com.example.newswave.presentation.Filter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val application: Application,
    private val newsDao: NewsDao,
    private val mapper: NewsMapper,
    private val apiService: ApiService
) : NewsRepository {


    @SuppressLint("NewApi")
    private var currentEndDate: LocalDate = LocalDate.now()


    override fun getNewsDetailsById(id: Int): Flow<NewsItemEntity> {
        return newsDao.getNewsDetailsById(id)
            .map { mapper.mapDbModelToEntity(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTopNewsList(): Flow<List<NewsItemEntity>> {
        return newsDao.getNewsList()
            .flatMapConcat { newsList ->
                flow {
                    emit(newsList.map { mapper.mapDbModelToEntity(it) })
                }
            }
    }


    override fun loadData() {
        val workManager = WorkManager.getInstance(application.applicationContext)
        workManager.enqueueUniqueWork(
            RefreshDataWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            RefreshDataWorker.makeRequest()
        )
    }

    @SuppressLint("NewApi")
    override suspend fun loadNewsForPreviousDay(){
        currentEndDate = currentEndDate.minusDays(1)
        val previousDate = currentEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val jsonContainer: Flow<TopNewsResponseDto> =
            apiService.getListTopNews(date = previousDate)
        val newsListDbModel =
            jsonContainer //преобразование из Flow<NewsResponseDto> в Flow<List<NewsItemDto>>
                .map { mapper.mapJsonContainerTopNewsToListNews(jsonContainer) }//преобразование в List<NewsItemDto>
                .flatMapConcat { newList ->
                    flow {
                        emit(newList.map { mapper.mapDtoToDbModel(it) }) //преобразование в List<NewsDbModel>
                    }
                }
                .flattenToList()// преобразование из flow в list
                .distinctBy { it.title }
        //преобразование в List<NewsDbModel>
        newsDao.insertNews(newsListDbModel)
        delay(10000)
    }

    override suspend fun searchNewsByFilter(filterParameter: String, valueParameter: String) {
        val news: List<NewsItemEntity> = when (filterParameter) {
            application.getString(Filter.TEXT.descriptionResId) -> apiService.getNewsByText(
                text = valueParameter
            )

            application.getString(Filter.AUTHOR.descriptionResId) -> apiService.getNewsByAuthor(
                author =  valueParameter
            )

            application.getString(Filter.DATE.descriptionResId) -> apiService.getNewsByDate(
                date = valueParameter
            )

            else -> {
                throw RuntimeException("error filter")
            }
        }
            .map { it.news }
            .flatMapConcat { newsList ->
                flow { emit(newsList.map { mapper.mapDtoToEntity(it) }) }
            }
            .flattenToList()
            .distinctBy { it.title }


        val sharedPreferences =
            application.getSharedPreferences("news_by_search", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("news_search_result", Gson().toJson(news))
        editor.apply()
    }

    override suspend fun getSavedNewsBySearch(): List<NewsItemEntity> {
        val sharedPreferences =
            application.getSharedPreferences("news_by_search", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("news_search_result", null)
        return if (json != null) {
            Gson().fromJson(json, object : TypeToken<List<NewsItemEntity>>() {}.type)
        } else {
            emptyList()
        }
    }

}