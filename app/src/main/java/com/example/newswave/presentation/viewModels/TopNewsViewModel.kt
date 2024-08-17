package com.example.newswave.presentation.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.repository.NewsRepositoryImpl
import com.example.newswave.domain.NewsInfo
import com.example.newswave.domain.usecases.GetNewsDetailsById
import com.example.newswave.domain.usecases.GetTopNewsList
import com.example.newswave.domain.usecases.LoadDataUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class TopNewsViewModel(application: Application): AndroidViewModel(application) {
    private val repository = NewsRepositoryImpl(application)
    private val loadDataUseCase = LoadDataUseCase(repository)
    private val getTopNewsListUseCase = GetTopNewsList(repository)
    private val getNewsDetailsByIdUseCase = GetNewsDetailsById(repository)

    val newsList = getTopNewsListUseCase().asLiveData()

    fun getDetailInfo(id: Int) = getNewsDetailsByIdUseCase(id)
    fun getTopNewsList() = getTopNewsListUseCase()


    init {
        viewModelScope.launch {
            loadDataUseCase()
        }
    }
}