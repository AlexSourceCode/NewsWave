package com.example.newswave.domain.model

import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.entity.NewsItemEntity

sealed class AuthorState {
    data class Error(val message: String, val timestamp: Long = System.currentTimeMillis()): AuthorState()
    object Loading: AuthorState()
    data class Success(val currentList: List<AuthorItemEntity>?): AuthorState()
}

//data class Error(
//    val throwable: Throwable? = null
//) : UiState