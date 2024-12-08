package com.example.newswave.presentation.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.R
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.presentation.state.NewsState
import com.example.newswave.domain.usecases.subscription.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.news.FetchErrorLoadDataUseCase
import com.example.newswave.domain.usecases.news.FetchTopNewsListUseCase
import com.example.newswave.domain.usecases.news.LoadDataUseCase
import com.example.newswave.domain.usecases.news.LoadNewsForPreviousDayUseCase
import com.example.newswave.domain.usecases.news.SearchNewsByFilterUseCase
import com.example.newswave.domain.usecases.news.SearchNewsByFilterUseCaseFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject


class TopNewsViewModel @Inject constructor(
    private val application: Application,
    var savedStateHandle: SavedStateHandle,
    private val loadDataUseCase: LoadDataUseCase,
    private val loadNewsForPreviousDayUseCase: LoadNewsForPreviousDayUseCase,
    private val fetchTopNewsListUseCase: FetchTopNewsListUseCase,
    private val searchNewsByFilterUseCaseFactory: SearchNewsByFilterUseCaseFactory,
    private val fetchErrorLoadDataUseCase: FetchErrorLoadDataUseCase,
    private val favoriteAuthorCheckUseCase: FavoriteAuthorCheckUseCase
) : ViewModel() {

    private lateinit var searchNewsByFilterUseCase: SearchNewsByFilterUseCase

    private val _uiState = MutableStateFlow<NewsState>(NewsState.Loading)
    val uiState: StateFlow<NewsState> = _uiState.asStateFlow()

    private val _searchTrigger = MutableStateFlow(false)
    private val _searchArgs = MutableStateFlow<Pair<String, String>?>(null)

    var isFirstLaunch = true // crutch


    init {
        loadData()
        fetchErrorLoadData()
        fetchTopNewsList()
        setupSearchTrigger()
        setupSearchArgs()
    }


    fun updateSearchParameters(filter: String, value: String) { // no
        viewModelScope.launch {
            _uiState.value = NewsState.Loading
            _searchArgs.value = Pair(filter, value)
            searchNewsByFilter()
        }
    }

    fun preloadAuthorData(author: String) {
        viewModelScope.launch {
            favoriteAuthorCheckUseCase(author)
        }
    }

    fun searchNewsByFilter() {
        if (!_searchTrigger.value) {
            _searchTrigger.value = true
            return
        }
        viewModelScope.launch {
            searchNewsByFilterUseCase()
        }
    }

    fun backToTopNews() {
        viewModelScope.launch {
            _uiState.value = NewsState.Success(fetchTopNewsListUseCase().value)
        }
    }


    fun loadNewsForPreviousDay() {
        viewModelScope.launch {
            loadNewsForPreviousDayUseCase()
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = NewsState.Loading
            Log.d("refreshDataState", "execute")
            loadDataUseCase()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            loadDataUseCase()
        }
    }

    private fun fetchErrorLoadData() {
        viewModelScope.launch {
            fetchErrorLoadDataUseCase()
                .collect { errorMessage ->
                    val trimmedErrorMessage = errorMessage.toString().trim()
                    Log.d("CheckErrorMessage", "get error: $errorMessage")
                    Log.d("CheckErrorMessage", errorMessage)
                    _uiState.value = NewsState.Error(errorMessage)
                    val savedNews = getTopNews()
                    if ((!savedNews.isNullOrEmpty()) && (trimmedErrorMessage != application.getString(
                            R.string.error_loading_news_by_filter
                        )) && (trimmedErrorMessage != application.getString(R.string.news_list_is_empty_or_invalid_parameters))
                        && (trimmedErrorMessage != application.getString(R.string.errorMessageNoResultsFound))
                    ) {
                        Log.d("CheckErrorMessage", "wtf ${errorMessage}")
                        Log.d("CheckErrorMessage", "wtf resource ${application.getString(R.string.errorMessageNoResultsFound)}")
                        _uiState.value = NewsState.Success(savedNews)
                    }
                }
        }
    }

    private fun fetchTopNewsList() {
        viewModelScope.launch {
            try {
                fetchTopNewsListUseCase()
                    .collect { news ->
                        Log.d("fetchTopNewsListUseCase", "execute collect")
                        if (news.isEmpty()) _uiState.value = NewsState.Loading
                        else {
                            _uiState.value = NewsState.Success(news)
                            saveTopNews(news)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = NewsState.Error(e.toString())
                val savedNews = getTopNews()
                if (!savedNews.isNullOrEmpty()) {
                    _uiState.value = NewsState.Success(savedNews)
                }
            }
        }
    }

    private fun getTopNews(): List<NewsItemEntity>? {
        return savedStateHandle["top_news"]
    }

    private fun saveTopNews(newsList: List<NewsItemEntity>) {
        savedStateHandle["top_news"] = newsList
    }

    private fun setupSearchTrigger() {
        viewModelScope.launch {
            try {
                _searchTrigger
                    .filter { it }
                    .take(1)
                    .collect {
                        searchNewsByFilterUseCase()
                            .collect { news ->
                                _uiState.value = NewsState.Success(news)
                            }
                    }
            } catch (e: Exception) {
                _uiState.value = NewsState.Error(e.toString())
            }
        }
    }

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