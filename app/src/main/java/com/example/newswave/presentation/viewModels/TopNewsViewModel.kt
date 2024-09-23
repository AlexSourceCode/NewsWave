package com.example.newswave.presentation.viewModels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.repository.NewsRepositoryImpl
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.usecases.GetTopNewsListUseCase
import com.example.newswave.domain.usecases.LoadDataUseCase
import com.example.newswave.domain.usecases.LoadNewsForPreviousDayUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCaseFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import javax.inject.Inject


class TopNewsViewModel @Inject constructor(
    private val loadDataUseCase: LoadDataUseCase,
    private val loadNewsForPreviousDayUseCase: LoadNewsForPreviousDayUseCase,
    private val getTopNewsListUseCase: GetTopNewsListUseCase,
    private val searchNewsByFilterUseCaseFactory: SearchNewsByFilterUseCaseFactory,
    private val sharedPreferences: SharedPreferences
): ViewModel() {

    private lateinit var searchNewsByFilterUseCase: SearchNewsByFilterUseCase

    private val _newsList = MutableLiveData<List<NewsItemEntity>>()
    val newsList: LiveData<List<NewsItemEntity>> get() = _newsList

    fun setSearchParameters(filterParameter: String, valueParameter: String) {
        searchNewsByFilterUseCase = searchNewsByFilterUseCaseFactory.create(filterParameter, valueParameter)
    }

    suspend fun searchNewsByFilter() {
        searchNewsByFilterUseCase()
    }

    fun showNews() {
        val newsSearchResult = sharedPreferences.getString("news_search_result", null)
        if (newsSearchResult != null) {
            val type = object : TypeToken<List<NewsItemEntity>>() {}.type
            val listFromDb: List<NewsItemEntity> = Gson().fromJson(newsSearchResult, type)
            _newsList.value = listFromDb
        }
    }

    fun loadTopNewsFromRoom(){
        viewModelScope.launch {
            getTopNewsListUseCase().collect{ news ->
                _newsList.postValue(news)
            }
        }
    }

    fun loadNewsForPreviousDay(){
        viewModelScope.launch {
            loadNewsForPreviousDayUseCase()
        }
    }



    init {
        loadDataUseCase()
        viewModelScope.launch {
            getTopNewsListUseCase().collect { news ->
                _newsList.postValue(news)
            }
        }
    }

}

