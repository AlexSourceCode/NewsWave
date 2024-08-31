package com.example.newswave.data.repository

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.example.newswave.data.database.dbNews.NewsDb
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.mapper.flattenToList
import com.example.newswave.data.network.api.ApiFactory
import com.example.newswave.data.network.model.NewsResponseDto
import com.example.newswave.data.network.model.TopNewsResponseDto
import com.example.newswave.domain.NewsItemEntity
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NewsRepositoryImpl(
    private val application: Application,
) : NewsRepository {


    private val newsInfoDao = NewsDb.getInstance(application)
    private val mapper = NewsMapper()
    private val apiService = ApiFactory.apiService

    override fun getNewsDetailsById(id: Int): Flow<NewsItemEntity> {
        return newsInfoDao.newsDao().getNewsDetailsById(id)
            .map { mapper.dbModelToEntity(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getTopNewsList(): Flow<List<NewsItemEntity>> {
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
            val jsonContainer: Flow<TopNewsResponseDto> =
                apiService.getListTopNews(date = formatDate()) // Временно другая дата, так как сегодня еще нет новостей
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
            newsInfoDao.newsDao().insertNews(newsListDbModel)
        } catch (e: Exception) {
            Log.e("CheckNews", "Error fetching top news", e)
        }
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

    @SuppressLint("NewApi")
    fun formatDate(): String {
        val currentDateTime = LocalDateTime.now()//текущая дата время
        val dateFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd") // шаблон отображения времени
        return currentDateTime.format(dateFormatted) //преобразование LocalDateTime в String по шаблону отображения
    }


}