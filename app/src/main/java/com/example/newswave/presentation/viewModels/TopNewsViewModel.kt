package com.example.newswave.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.model.NewsState
import com.example.newswave.domain.usecases.FetchTopNewsListUseCase
import com.example.newswave.domain.usecases.LoadDataUseCase
import com.example.newswave.domain.usecases.LoadNewsForPreviousDayUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCaseFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import javax.inject.Inject


class TopNewsViewModel @Inject constructor(
    private val loadDataUseCase: LoadDataUseCase,
    private val loadNewsForPreviousDayUseCase: LoadNewsForPreviousDayUseCase,
    private val fetchTopNewsListUseCase: FetchTopNewsListUseCase,
    private val searchNewsByFilterUseCaseFactory: SearchNewsByFilterUseCaseFactory,
) : ViewModel() {

    private lateinit var searchNewsByFilterUseCase: SearchNewsByFilterUseCase

    private val _uiState = MutableStateFlow<NewsState>(NewsState.Loading)
    val uiState: StateFlow<NewsState> = _uiState.asStateFlow()

    private val _searchTrigger = MutableStateFlow<Boolean>(false)

    fun updateSearchParameters(filter: String, value: String) {
        searchNewsByFilterUseCase =
            searchNewsByFilterUseCaseFactory.create(filter, value)
    }

    fun searchNewsByFilter() {
        _searchTrigger.value = true
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
        loadDataUseCase()
    }

    init {
        loadDataUseCase()

        viewModelScope.launch {
            try {
                fetchTopNewsListUseCase()
                    .collect { news ->
                        _uiState.value = NewsState.Success(news)
                    }
            } catch (e: Exception) {
                _uiState.value = NewsState.Error(e.toString())
            }
        }

        viewModelScope.launch {
            try {
                _searchTrigger
                    .filter { it }
                    .take(1)
                    .collect{
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
}