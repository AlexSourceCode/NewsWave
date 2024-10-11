package com.example.newswave.presentation.viewModels

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


    fun updateSearchParameters(filter: String, value: String) {
        searchNewsByFilterUseCase =
            searchNewsByFilterUseCaseFactory.create(filter, value)
    }

    fun searchNewsByFilter() {
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
        updateSearchParameters("initial", "initial") // crutch!

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

        viewModelScope.launch {  // почему в разных скоупах
            try {
                searchNewsByFilterUseCase()
                    .collect { news ->
                        _uiState.value = NewsState.Success(news)
                    }
            } catch (e: Exception) {
                _uiState.value = NewsState.Error(e.toString())
            }
        }
    }
}