package com.example.newswave.presentation.viewModels

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
):ViewModel() {

    private val _uiState = MutableStateFlow<NewsState>(NewsState.Loading)
    val uiState: StateFlow<NewsState> get() = _uiState

    fun refreshData(author: String){
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
                    .distinctUntilChanged()
                    .collect{ news ->
                        _uiState.value = NewsState.Success(news) }
            } catch (e: Exception) {
                _uiState.value = NewsState.Error(e.toString())
            }
        }
    }
}