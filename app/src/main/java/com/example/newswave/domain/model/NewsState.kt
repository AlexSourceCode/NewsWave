package com.example.newswave.domain.model

import com.example.newswave.domain.entity.NewsItemEntity

sealed class NewsState {

    data class Error(val message: String): NewsState()
    object Loading: NewsState()
    data class Success(val currentList: List<NewsItemEntity>): NewsState()
}

//data class Error(
//    val throwable: Throwable? = null
//) : UiState