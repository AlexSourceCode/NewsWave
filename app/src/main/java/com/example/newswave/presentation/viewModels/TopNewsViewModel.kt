package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.model.State
import com.example.newswave.domain.usecases.GetTopNewsListUseCase
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
    private val getTopNewsListUseCase: GetTopNewsListUseCase,
    private val searchNewsByFilterUseCaseFactory: SearchNewsByFilterUseCaseFactory,
) : ViewModel() {

    private lateinit var searchNewsByFilterUseCase: SearchNewsByFilterUseCase

    private val _uiState = MutableStateFlow<State>(State.Loading)
    val uiState: StateFlow<State> = _uiState.asStateFlow()



    fun setSearchParameters(filterParameter: String, valueParameter: String) {
        searchNewsByFilterUseCase =
            searchNewsByFilterUseCaseFactory.create(filterParameter, valueParameter)
    }

    fun searchNewsByFilter() {
        viewModelScope.launch {
            _uiState.value = State.Success(searchNewsByFilterUseCase())
        }
    }

    fun backToTopNews(){
        viewModelScope.launch {
            _uiState.value = State.Success(getTopNewsListUseCase().value)
        }
    }


    fun loadNewsForPreviousDay() {
        viewModelScope.launch {
            loadNewsForPreviousDayUseCase()
        }
    }


    init {
        loadDataUseCase()

        viewModelScope.launch {
            try {
                getTopNewsListUseCase()
                    .collect { news ->
                        _uiState.value = State.Success(news) }
            } catch (e: Exception) {
                _uiState.value = State.Error(e.toString())
            }
        }
    }
}