package com.example.newswave.domain.model

import com.example.newswave.domain.entity.NewsItemEntity

sealed class State {

    data class Error(val message: String): State()
    object Loading: State()
    data class Success(val currentList: List<NewsItemEntity>): State()
}