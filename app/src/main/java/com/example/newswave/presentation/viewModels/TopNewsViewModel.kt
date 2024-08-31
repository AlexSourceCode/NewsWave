package com.example.newswave.presentation.viewModels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.mapper.flattenToList
import com.example.newswave.data.repository.NewsRepositoryImpl
import com.example.newswave.domain.NewsItemEntity
import com.example.newswave.domain.usecases.GetNewsDetailsById
import com.example.newswave.domain.usecases.GetSavedNewsBySearchUseCase
import com.example.newswave.domain.usecases.GetTopNewsList
import com.example.newswave.domain.usecases.LoadDataUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch


class TopNewsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = NewsRepositoryImpl(application)
    private val loadDataUseCase = LoadDataUseCase(repository)
    private val getTopNewsListUseCase = GetTopNewsList(repository)
    private val getNewsDetailsByIdUseCase = GetNewsDetailsById(repository)
    private lateinit var searchNewsByFilterUseCase: SearchNewsByFilterUseCase

    //    val newsList = getTopNewsListUseCase().asLiveData()
    private val _newsList = MutableLiveData<List<NewsItemEntity>>()
    val newsList: LiveData<List<NewsItemEntity>> get() = _newsList


    fun getDetailInfo(id: Int) = getNewsDetailsByIdUseCase(id)
    fun getTopNewsList() = getTopNewsListUseCase()

    fun setSearchParameters(filterParameter: String, valueParameter: String) {
        searchNewsByFilterUseCase =
            SearchNewsByFilterUseCase(filterParameter, valueParameter, repository)
    }

    suspend fun searchNewsByFilter() {
        searchNewsByFilterUseCase()
    }

    suspend fun showNews() {
        val sharedPreferences =
            getApplication<Application>().applicationContext.getSharedPreferences(
                "news_by_search",
                Context.MODE_PRIVATE
            )
        val newsSearchResult = sharedPreferences.getString("news_search_result", null)
        if (newsSearchResult != null) {
            val type = object : TypeToken<List<NewsItemEntity>>() {}.type
            val newsListP: List<NewsItemEntity> = Gson().fromJson(newsSearchResult, type)
            _newsList.value = newsListP
        }
    }

    fun loadTopNewsFromRoom(){
        viewModelScope.launch {
            getTopNewsListUseCase().collect{
                news ->
                _newsList.postValue(news)
            }
        }
    }


    init {
        viewModelScope.launch {
            loadDataUseCase()
            getTopNewsListUseCase().collect { news ->
                _newsList.postValue(news)
            }
        }
    }

}

