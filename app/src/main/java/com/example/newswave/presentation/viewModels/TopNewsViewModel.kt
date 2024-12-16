package com.example.newswave.presentation.viewModels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.usecases.news.FetchErrorLoadDataUseCase
import com.example.newswave.domain.usecases.news.FetchTopNewsListUseCase
import com.example.newswave.domain.usecases.news.LoadDataUseCase
import com.example.newswave.domain.usecases.news.LoadNewsForPreviousDayUseCase
import com.example.newswave.domain.usecases.news.SearchNewsByFilterUseCase
import com.example.newswave.domain.usecases.news.SearchNewsByFilterUseCaseFactory
import com.example.newswave.domain.usecases.subscription.FavoriteAuthorCheckUseCase
import com.example.newswave.presentation.state.NewsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * TopNewsViewModel отвечает за управление состоянием и бизнес-логикой для экрана топ-новостей.
 */
class TopNewsViewModel @Inject constructor(
    var savedStateHandle: SavedStateHandle,
    private val loadDataUseCase: LoadDataUseCase,
    private val loadNewsForPreviousDayUseCase: LoadNewsForPreviousDayUseCase,
    private val fetchTopNewsListUseCase: FetchTopNewsListUseCase,
    private val searchNewsByFilterUseCaseFactory: SearchNewsByFilterUseCaseFactory,
    private val fetchErrorLoadDataUseCase: FetchErrorLoadDataUseCase,
    private val favoriteAuthorCheckUseCase: FavoriteAuthorCheckUseCase,
) : ViewModel() {

    private lateinit var searchNewsByFilterUseCase: SearchNewsByFilterUseCase // Поздняя инициализация UseCase для поиска новостей

    // Хранит текущее состояние интерфейса новостей
    private val _uiState = MutableStateFlow<NewsState>(NewsState.Loading)
    val uiState: StateFlow<NewsState> = _uiState.asStateFlow()

    private val _searchTrigger = MutableStateFlow(false) // Триггер для запуска поиска
    private val _searchArgs = MutableStateFlow<Pair<String, String>?>(null) // Параметры поиска

    // Указывает, находится ли пользователь в режиме поиска
    private val _isInSearchMode = MutableStateFlow(false)
    val isInSearchMode: StateFlow<Boolean> = _isInSearchMode.asStateFlow()

    var isFirstLaunch = true // Флаг для отслеживания первого запуска (временный костыль)

    var savedPosition: Int = 0
    var savedOffset: Int = 0

    init {
        loadData()
        fetchErrorLoadData()
        fetchTopNewsList()
        setupSearchTrigger()
        setupSearchArgs()
    }

    // Обновляет параметры поиска и запускает поиск
    fun updateSearchParameters(filter: String, value: String) { // no
        viewModelScope.launch {
            _uiState.value = NewsState.Loading
            _searchArgs.value = Pair(filter, value)
            _isInSearchMode.value = true // Вход в режим поиска
            searchNewsByFilter()
        }
    }

    // Предзагрузка данных по автору
    fun preloadAuthorData(author: String) {
        viewModelScope.launch {
            favoriteAuthorCheckUseCase(author)
        }
    }

    // Выполняет поиск новостей по заданным параметрам
    fun searchNewsByFilter() {
        if (!_searchTrigger.value) {
            _searchTrigger.value = true
            return
        }
        viewModelScope.launch {
            searchNewsByFilterUseCase()
        }
    }

    // Возвращает интерфейс в режим отображения топ-новостей
    fun backToTopNews() {
        viewModelScope.launch {
            Log.d("TopNewsViewModel", "backToTopNews")
            _uiState.value = NewsState.Success(fetchTopNewsListUseCase().value)
            _isInSearchMode.value = false // Выход из режима поиска
        }
    }

    // Загружает новости за предыдущий день
    fun loadNewsForPreviousDay() {
        viewModelScope.launch {
            loadNewsForPreviousDayUseCase()
        }
    }

    // Обновляет данные
    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = NewsState.Loading
            loadDataUseCase()
        }
    }

    // Показывает сохраненные топ-новости, если они доступны
    fun showTopNews(){
        val savedNews = getTopNews()
        if (!savedNews.isNullOrEmpty()) {
            Log.d("TopNewsViewModel", "showTopNews")
            _uiState.value = NewsState.Success(savedNews)
        }
    }

    // Загружает основные данные
    private fun loadData() {
        viewModelScope.launch {
            loadDataUseCase()
        }
    }

    // Подписка на сообщения об ошибках загрузки данных
    private fun fetchErrorLoadData() {
        viewModelScope.launch {
            fetchErrorLoadDataUseCase()
                .collect { errorMessage -> _uiState.value = NewsState.Error(errorMessage) }
        }
    }

    // Получает список топ-новостей
    private fun fetchTopNewsList() {
        viewModelScope.launch {
            try {
                fetchTopNewsListUseCase()
                    .collect { news ->
                        if (news.isEmpty()) _uiState.value = NewsState.Loading
                        else {
                            Log.d("TopNewsViewModel", "fetchTopNewsList")
                            Log.d("TopNewsViewModel", news.get(0).title.toString())
                            _uiState.value = NewsState.Success(news)
                            saveTopNews(news)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = NewsState.Error(e.toString())
            }
        }
    }

    // Получает сохраненные топ-новости из SavedStateHandle
    private fun getTopNews(): List<NewsItemEntity>? {
        return savedStateHandle["top_news"]
    }

    // Сохраняет список топ-новостей в SavedStateHandle
    private fun saveTopNews(newsList: List<NewsItemEntity>) {
        savedStateHandle["top_news"] = newsList
    }

    // Настраивает обработку триггера для выполнения поиска
    private fun setupSearchTrigger() {
        viewModelScope.launch {
            try {
                _searchTrigger
                    .filter { it }
                    .take(1)
                    .collect {
                        searchNewsByFilterUseCase()
                            .collect { news ->
                                Log.d("TopNewsViewModel", "setupSearchTrigger")
                                _uiState.value = NewsState.Success(news)
                            }
                    }
            } catch (e: Exception) {
                _uiState.value = NewsState.Error(e.toString())
            }
        }
    }

    // Настраивает обработку параметров поиска
    private fun setupSearchArgs() {
        viewModelScope.launch {
            _searchArgs
                .filterNotNull()
                .collect { args ->
                    searchNewsByFilterUseCase =
                        searchNewsByFilterUseCaseFactory.create(args.first, args.second)
                }
        }
    }
}