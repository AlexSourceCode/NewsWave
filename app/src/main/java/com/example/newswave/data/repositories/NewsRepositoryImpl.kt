package com.example.newswave.data.repositories

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.newswave.R
import com.example.newswave.data.source.local.UserPreferences
import com.example.newswave.data.worker.RefreshDataWorker
import com.example.newswave.domain.entities.NewsItemEntity
import com.example.newswave.domain.model.Filter
import com.example.newswave.domain.repositories.LocalDataSource
import com.example.newswave.domain.repositories.NewsRepository
import com.example.newswave.domain.repositories.RemoteDataSource
import com.example.newswave.utils.LocaleHelper
import com.example.newswave.utils.NetworkUtils.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.CancellationException
import javax.inject.Inject

/**
Реализация репозитория для управления данными новостей
Позволяет загружать, фильтровать и обновлять данные
 */
class NewsRepositoryImpl @Inject constructor(
    private val application: Application,
    private val localDataSource: LocalDataSource, // Локальный источник данных (БД)
    private val remoteDataSource: RemoteDataSource, // Удалённый источник данных (API),
    private val userPreferences: UserPreferences, // Локальные пользовательские настройки
) : NewsRepository {

    // Текущая дата для фильтрации новостей по дням
    @SuppressLint("NewApi")
    private var currentEndDate: LocalDate = LocalDate.now()

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Поток для передачи фильтров и значений
    private val _filterFlow =
        MutableSharedFlow<Pair<String, String>>()

    // Поток отфильтрованных новостей
    private val _filteredNewsSharedFlow = MutableSharedFlow<List<NewsItemEntity>>()
    private val filteredNewsSharedFlow: SharedFlow<List<NewsItemEntity>> get() = _filteredNewsSharedFlow.asSharedFlow()

    // Поток для передачи ошибок
    private val _observeErrorLoadData = MutableSharedFlow<String>()
    private val observeErrorLoadData: SharedFlow<String> get() = _observeErrorLoadData.asSharedFlow()

    // Поток для получения списка новостей из БД
    private val topNewsStateFlow = localDataSource.getNewsList()

    init {
        observeFilterFlow()
    }

    // Получение топ новостей из БД
    override suspend fun fetchTopNewsList(): StateFlow<List<NewsItemEntity>> = topNewsStateFlow

    // Запуск фоновой задачи для обновления данных через WorkManager
    override suspend fun loadData() {
        val workManager = WorkManager.getInstance(application.applicationContext)
        val workRequest = RefreshDataWorker.makeRequest()

        workManager.enqueueUniqueWork(
            RefreshDataWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        observeWorkState(workManager)
    }

    // Отслеживание состояния задачи WorkManager
    private suspend fun observeWorkState(workManager: WorkManager) {
        workManager.getWorkInfosForUniqueWorkFlow(RefreshDataWorker.WORK_NAME)
            .collect { workInfos ->
                workInfos.firstOrNull()?.let { handleWorkState(it) }
            }
    }

    // Обработка состояний задачи WorkManager
    private suspend fun handleWorkState(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> {
                if (!isNetworkAvailableWithError()) throw CancellationException("No network available")
            }

            WorkInfo.State.FAILED -> {
                val error = workInfo.outputData.getString("error")
                _observeErrorLoadData.emit(error.toString())
                throw CancellationException("Work failed")
            }

            WorkInfo.State.SUCCEEDED -> {
                Log.d("NewsRepositoryImpl", "Work succeeded")
            }

            else -> Unit
        }
    }

    // Поток для ошибок при загрузке данных
    override suspend fun fetchErrorLoadData(): SharedFlow<String> = observeErrorLoadData

    // Загрузка новостей за предыдущий день.
    @SuppressLint("NewApi")
    override suspend fun loadNewsForPreviousDay() {
        currentEndDate = currentEndDate.minusDays(1)
        val previousDate = currentEndDate.format(
            DateTimeFormatter.ofPattern(application.getString(R.string.yyyy_mm_dd))
        )
        fetchDataWithNetworkCheck {
            val newsList = remoteDataSource.fetchTopNews(previousDate)
            localDataSource.insertNews(newsList)
        }
    }

    // Универсальная функция для проверки сети перед выполнением операции
    private suspend fun fetchDataWithNetworkCheck(onFetch: suspend () -> Unit) {
        if (isNetworkAvailableWithError()) {
            try {
                onFetch()
            } catch (e: Exception) {
                _observeErrorLoadData.emit(e.message.toString())
            }
        }
    }

    // Поиск новостей по заданному фильтру
    override suspend fun searchNewsByFilter(
        filterParameter: String,
        valueParameter: String
    ): SharedFlow<List<NewsItemEntity>> {
        _filterFlow.emit(filterParameter to valueParameter)
        return filteredNewsSharedFlow
    }

    override suspend fun getInterfaceLanguage(): String {
        return userPreferences.getInterfaceLanguage()
    }

    // Проверка доступности сети с отправкой ошибок при отсутствии соединения
    private suspend fun isNetworkAvailableWithError(): Boolean {
        return if (!isNetworkAvailable(application)) {
            _observeErrorLoadData.emit(
                application.getString(R.string.no_internet_connection)
            )
            false
        } else {
            true
        }
    }

    // Применение фильтра для получения новостей
    private suspend fun applyFilter(filter: Filter, value: String): Flow<List<NewsItemEntity>?> {
        return remoteDataSource.fetchFilteredNews(filter, value)
    }

    // Получение типа фильтра
    private suspend fun getFilterType(filter: String): Filter {
        val interfaceLanguage = getInterfaceLanguage()
        val localeContext = getLocalizedContext(interfaceLanguage)

        val map = mapOf(
            localeContext.getString(Filter.TEXT.descriptionResId) to Filter.TEXT,
            localeContext.getString(Filter.AUTHOR.descriptionResId) to Filter.AUTHOR,
            localeContext.getString(Filter.DATE.descriptionResId) to Filter.DATE
        )
        return map[filter] ?: throw IllegalArgumentException(
            application.getString(
                R.string.invalid_filter,
                filter
            )
        )
    }

    private fun getLocalizedContext(language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = application.resources.configuration
        config.setLocale(locale)

        return application.createConfigurationContext(config)
    }

    // Наблюдение за потоком фильтров и выполнение поиска
    private fun observeFilterFlow() {
        ioScope.launch {
            _filterFlow
                .filter { isNetworkAvailableWithError() }
                .flatMapLatest { (filterParameter, filterValue) ->
                    val filterType = getFilterType(filterParameter)
                    applyFilter(filterType, filterValue)
                        .catch { e ->
                            _observeErrorLoadData.emit(e.message.toString())
                            emit(null)
                        }
                        .filterNotNull()
                }
                .collect { news ->
                    if (news.isEmpty()) {
                        _observeErrorLoadData.emit(
                            application.getString(R.string.errorMessageNoResultsFound)
                        )
                        return@collect
                    }
                    _filteredNewsSharedFlow.emit(news)
                }
        }
    }

    override fun clear() {
        ioScope.cancel()
    }
}