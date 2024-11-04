package com.example.newswave.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.model.NewsState
import com.example.newswave.domain.usecases.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.FetchErrorLoadDataUseCase
import com.example.newswave.domain.usecases.FetchTopNewsListUseCase
import com.example.newswave.domain.usecases.IsFavoriteAuthorUseCase
import com.example.newswave.domain.usecases.LoadDataUseCase
import com.example.newswave.domain.usecases.LoadNewsForPreviousDayUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCaseFactory
import kotlinx.coroutines.delay
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
            _searchArgs.value = Pair(filter,value)
            searchNewsByFilter()
        }
    }

    fun preloadAuthorData(author: String){
        viewModelScope.launch {
            favoriteAuthorCheckUseCase(author)
        }
    }

    fun searchNewsByFilter() {
        if (!_searchTrigger.value){
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
            loadDataUseCase()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            loadDataUseCase()
        }
    }

    private fun fetchErrorLoadData(){
        viewModelScope.launch {
            fetchErrorLoadDataUseCase()
                .collect{
                    _uiState.value = NewsState.Error(it) // дважды одно и тоже значение
                }
        }
    }

    private fun fetchTopNewsList(){ //yes
        viewModelScope.launch {
            delay(500)
            try {
                fetchTopNewsListUseCase()
                    .collect { news ->
                        if (news.isEmpty()) _uiState.value = NewsState.Loading
                        else _uiState.value = NewsState.Success(news)
                    }
            } catch (e: Exception) {
                _uiState.value = NewsState.Error(e.toString())
            }
        }
    }

    private fun setupSearchTrigger(){
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

    private fun setupSearchArgs(){
        viewModelScope.launch {
            _searchArgs
                .filterNotNull()
                .collect{ args ->
                    searchNewsByFilterUseCase =
                        searchNewsByFilterUseCaseFactory.create(args.first, args.second)
                }
        }
    }

}