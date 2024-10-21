package com.example.newswave.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.model.NewsState
import com.example.newswave.domain.usecases.LoadAuthorNewsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthorNewsViewModel @Inject constructor(
    private val loadAuthorNewsUseCase: LoadAuthorNewsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsState>(NewsState.Loading)
    val uiState: StateFlow<NewsState> get() = _uiState

    fun refreshData(author: String) {
        viewModelScope.launch {
            try {
                loadAuthorNewsUseCase(author)
            } catch (e: Exception) {
                _uiState.value = NewsState.Error(e.toString())
            }
        }
    }

    fun loadAuthorNews(author: String) {
        viewModelScope.launch {
            try {
                loadAuthorNewsUseCase(author)
                    .distinctUntilChanged()//mb comment
                    .collect { news ->
                        _uiState.value = when (news) {
                            is NewsState.Error -> NewsState.Error(news.message)
                            is NewsState.Loading -> NewsState.Loading
                            is NewsState.Success -> NewsState.Success(news.currentList)
                        }
                    }
            } catch (e: Exception){
                Log.d("AuthorNewsViewModel", e.toString())
            }
        }
    }
}