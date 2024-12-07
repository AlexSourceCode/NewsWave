package com.example.newswave.domain.model

import com.example.newswave.domain.entity.NewsItemEntity

/**
 * Представляет состояние пользовательского интерфейса для данных о новостях
 */
sealed class NewsState {
    data class Error(val message: String, val timestamp: Long = System.currentTimeMillis()) :
        NewsState()

    object Loading : NewsState()
    data class Success(val currentList: List<NewsItemEntity>) : NewsState()
}