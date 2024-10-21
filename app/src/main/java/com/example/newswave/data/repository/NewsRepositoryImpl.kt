package com.example.newswave.data.repository

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import retrofit2.HttpException
import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.mapper.flattenToList
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.network.model.NewsItemDto
import com.example.newswave.data.network.model.TopNewsResponseDto
import com.example.newswave.data.workers.RefreshDataWorker
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.repository.NewsRepository
import com.example.newswave.utils.Filter
import com.example.newswave.utils.NetworkUtils.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val _filterFlow = MutableSharedFlow<String>()
    private var _valueFilter: String? = null

    private val _filteredNewsFlow = MutableSharedFlow<List<NewsItemEntity>>()
    private val filteredNewsFlow: SharedFlow<List<NewsItemEntity>> get() = _filteredNewsFlow.asSharedFlow()

    private val _errorLoadData = MutableSharedFlow<String>()
    private val errorLoadData: SharedFlow<String> get() = _errorLoadData.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val topNewsFlow = newsDao.getNewsList()
        .flatMapConcat { newsList ->
            flow {
                emit(newsList.map { mapper.mapDbModelToEntity(it) })
            }
        }.stateIn(
            scope = ioScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )


    override suspend fun fetchTopNewsList(): StateFlow<List<NewsItemEntity>> = topNewsFlow

    override suspend fun loadData() {
        val workManager =
            WorkManager.getInstance(application.applicationContext)     // Получаем экземпляр WorkManager для управления задачами
        val workRequest =
            RefreshDataWorker.makeRequest()     // Создаем запрос на выполнение работы

        if (workRequest != null) {
            workManager.enqueueUniqueWork(
                RefreshDataWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            ioScope.launch {
                workManager.getWorkInfosForUniqueWorkFlow(RefreshDataWorker.WORK_NAME)
                    .collect { workInfos ->
                        val workInfo = workInfos.firstOrNull()
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.ENQUEUED -> {
                                    if (!isNetworkAvailable(application)) {
                                        _errorLoadData.emit("No Internet connection")
                                    }
                                }

                                WorkInfo.State.FAILED -> {
                                    val error = workInfo.outputData.getString("error")
                                    if (error != null) {
                                        _errorLoadData.emit(error)
                                    }
                                }

                                WorkInfo.State.SUCCEEDED -> {
                                    cancel() // Прекращаем слежение при успешном завершении работы
                                }

                                else -> {}
                            }
                        }
                    }
            }
        }
    }


    override suspend fun fetchErrorLoadData(): SharedFlow<String> = errorLoadData


    @SuppressLint("NewApi")
    override suspend fun loadNewsForPreviousDay() {
        ioScope.launch {
            currentEndDate = currentEndDate.minusDays(1)
            val previousDate = currentEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            if (!isNetworkAvailable(application)) {
                _errorLoadData.emit("No Internet connection")
                return@launch
            }
            try {
                apiService.getListTopNews(date = previousDate)
                    .map { mapper.mapJsonContainerTopNewsToListNews(flow { emit(it) }) }//преобразование в List<NewsItemDto>
                    .flatMapConcat { newList ->
                        flow {
                            emit(newList.map { mapper.mapDtoToDbModel(it) }) //преобразование в List<NewsDbModel>
                        }
                    }
                    .map { it.distinctBy { news -> news.title } }
                    .collect {
                        newsDao.insertNews(it)
                    }
            } catch (e: Exception){
                _errorLoadData.emit("Error loading news ${e.message}")
            }
        }
    }

    private suspend fun updateFilterParameters(filterParameter: String, valueParameter: String) {
        _valueFilter = valueParameter
        _filterFlow.emit(filterParameter)
    }

    override suspend fun searchNewsByFilter(
        filterParameter: String,
        valueParameter: String
    ): SharedFlow<List<NewsItemEntity>> {
        updateFilterParameters(filterParameter, valueParameter)
        return filteredNewsFlow
    }

    private suspend fun getNewsByText(text: String): Flow<List<NewsItemDto>> {
        try {
            return apiService.getNewsByText(text = text).map { it.news }
        } catch (e: Exception) {
            _errorLoadData.emit("Error loading news by text: ${e.message}")
            return flow { emptyList<NewsItemDto>() }
        }
    }

    private suspend fun getNewsByAuthor(author: String): Flow<List<NewsItemDto>> {
        try {
            return apiService.getNewsByAuthor(author = author).map { it.news }
        } catch (e: Exception) {
            _errorLoadData.emit("Error loading news by text: ${e.message}")
            return flow { emptyList<NewsItemDto>() }
        }
    }

    private suspend fun getNewsByDate(date: String): Flow<List<NewsItemDto>> {
        try {
            return apiService.getNewsByDate(date = date)
                .map { mapper.mapJsonContainerTopNewsToListNews(flow { emit(it) }) }
        } catch (e: Exception) {
            _errorLoadData.emit("Error loading news by text: ${e.message}")
            return flow { emptyList<NewsItemDto>() }
        }
    }

    init {
        ioScope.launch {
            _filterFlow
                .filter {
                    val isConnected = isNetworkAvailable(application)
                    if (!isConnected) {
                        _errorLoadData.emit("No Internet connection")
                    }
                    isConnected
                }
                .flatMapLatest { filter ->
                    when (filter) {
                        application.getString(Filter.TEXT.descriptionResId) ->
                            getNewsByText(_valueFilter.toString())

                        application.getString(Filter.AUTHOR.descriptionResId) ->
                            getNewsByAuthor(_valueFilter.toString())

                        application.getString(Filter.DATE.descriptionResId) ->
                            getNewsByDate(_valueFilter.toString())

                        else -> {
                            throw Exception("Invalid filter: $filter")
                        }
                    }
//                        .retry { cause ->
//                            if (cause is HttpException && cause.code() == 429) {
//                                delay(2000)
//                                true
//                            } else false
//                        }
                        .flatMapConcat { newsList ->
                            flow { emit(newsList.map { mapper.mapDtoToEntity(it) }) }
                        }
                        .map { it.distinctBy { news -> news.title } }
                        .map {
                            if (filter == application.getString(Filter.DATE.descriptionResId)) {
                                it.sortedBy { it.publishDate }
                            } else it
                        }
                }
                .collect { news -> _filteredNewsFlow.emit(news) }
        }
    }
}