package com.example.newswave.presentation.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.model.State
import com.example.newswave.domain.usecases.GetTopNewsListUseCase
import com.example.newswave.domain.usecases.LoadDataUseCase
import com.example.newswave.domain.usecases.LoadNewsForPreviousDayUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCaseFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject


class TopNewsViewModel @Inject constructor(
    private val loadDataUseCase: LoadDataUseCase,
    private val loadNewsForPreviousDayUseCase: LoadNewsForPreviousDayUseCase,
    private val getTopNewsListUseCase: GetTopNewsListUseCase,
    private val searchNewsByFilterUseCaseFactory: SearchNewsByFilterUseCaseFactory,
) : ViewModel() {

    private lateinit var searchNewsByFilterUseCase: SearchNewsByFilterUseCase

    private val _newsList = MutableLiveData<List<NewsItemEntity>>()
    val newsList: LiveData<List<NewsItemEntity>> get() = _newsList


    fun setSearchParameters(filterParameter: String, valueParameter: String) {
        searchNewsByFilterUseCase =
            searchNewsByFilterUseCaseFactory.create(filterParameter, valueParameter)
    }

    fun searchNewsByFilter() {
        viewModelScope.launch {
            _newsList.value = searchNewsByFilterUseCase()
        }
    }


    fun loadTopNewsFromRoom() {
        viewModelScope.launch {
            try {
                getTopNewsListUseCase()
                    .collect { news -> _newsList.value = news }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error loading news", e)
            }
        }
    }


    fun loadNewsForPreviousDay() {
        viewModelScope.launch {
            loadNewsForPreviousDayUseCase()
        }
    }


    init {
        loadDataUseCase()
        loadTopNewsFromRoom()
    }
}

//val stateTopNews: LiveData<State> = liveData {
//    viewModelScope.launch {
//        try {
//            getTopNewsListUseCase()
//                .map { State.Success(it) as State }
//                .onEach { emit(it) }
//                .onStart { emit(State.Loading) }
//        } catch (e: Exception) {
//            emit(State.Error(e.toString()))
//        }
//    }
//}
//val stateSearchNews: LiveData<State> by lazy {
//    liveData {
//        viewModelScope.launch {
//            emit(State.Loading)
//            try {
//                emit(State.Success(searchNewsByFilterUseCase()))
//            } catch (e: Exception) {
//                emit(State.Error(e.toString()))
//            }
//        }
//    }
//}


