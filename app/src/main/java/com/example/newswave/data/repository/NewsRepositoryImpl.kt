package com.example.newswave.data.repository

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
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
import com.example.newswave.utils.Filter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
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

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    val getTopNewsList = newsDao.getNewsList()
        .flatMapConcat { newsList ->
            flow {
                emit(newsList.map { mapper.mapDbModelToEntity(it) })
            }
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )



    override suspend fun getTopNewsList(): StateFlow<List<NewsItemEntity>> = getTopNewsList

    override fun loadData() {
        val workManager = WorkManager.getInstance(application.applicationContext)
        workManager.enqueueUniqueWork(
            RefreshDataWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            RefreshDataWorker.makeRequest()
        )
    }

    @SuppressLint("NewApi")
    override suspend fun loadNewsForPreviousDay() {//add withContext
        withContext(Dispatchers.IO) {
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
        }
        delay(10000)
    }

    override suspend fun searchNewsByFilter(
        filterParameter: String,
        valueParameter: String
    ): List<NewsItemEntity> =
        withContext(Dispatchers.IO) {
            when (filterParameter) {
                application.getString(Filter.TEXT.descriptionResId) -> apiService.getNewsByText(
                    text = valueParameter
                )

                application.getString(Filter.AUTHOR.descriptionResId) -> apiService.getNewsByAuthor(
                    author = valueParameter
                )

                application.getString(Filter.DATE.descriptionResId) -> apiService.getNewsByDate(
                    date = valueParameter
                )

                else -> {
                    throw RuntimeException("error filter")
                }
            }.retry {
                Log.d("RetryLoad", it.toString())
                delay(1000)
                true
            }
                .map { it.news }
                .flatMapConcat { newsList ->
                    flow { emit(newsList.map { mapper.mapDtoToEntity(it) }) }
                }
                .flattenToList()
                .distinctBy { it.title }
        }
}