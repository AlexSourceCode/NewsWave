package com.example.newswave.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.usecases.LoadAuthorNewsUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthorNewsViewModel @Inject constructor(
    private val loadAuthorNewsUseCase: LoadAuthorNewsUseCase,
):ViewModel() {

    private val _newsList = MutableLiveData<List<NewsItemEntity>>()
    val newsList: LiveData<List<NewsItemEntity>>
        get() = _newsList

    fun loadAuthorNews(author: String){
        viewModelScope.launch {
            val news = loadAuthorNewsUseCase(author)
            _newsList.value = news
        }
    }
}