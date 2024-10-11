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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Thread.State
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

    init {
        ioScope.launch {
            _filterFlow
                .filterNot { it == "initial" }
                .flatMapLatest { filter ->
                    when (filter) {
                        application.getString(Filter.TEXT.descriptionResId) -> apiService.getNewsByText(
                            text = _valueFilter.toString() // temp solution
                        )

                        application.getString(Filter.AUTHOR.descriptionResId) -> apiService.getNewsByAuthor(
                            author = _valueFilter.toString()
                        )

                        application.getString(Filter.DATE.descriptionResId) -> apiService.getNewsByDate(
                            date = _valueFilter.toString()
                        )

                        else -> {
                            throw RuntimeException("error filter")
                        }
                    }
                        .retry {
                            delay(1000)
                            true
                        }
                        .map { it.news }
                        .flatMapConcat { newsList ->
                            flow { emit(newsList.map { mapper.mapDtoToEntity(it) }) }
                        }
                        .map { it.distinctBy { news -> news.id } }
                }
                .collect { news ->
                    _filteredNewsFlow.emit(news)
                }
        }
    }
}