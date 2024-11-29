package com.example.newswave.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.model.AuthState
import com.example.newswave.domain.model.NewsState
import com.example.newswave.domain.usecases.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.IsFavoriteAuthorUseCase
import com.example.newswave.domain.usecases.LoadAuthorNewsUseCase
import com.example.newswave.domain.usecases.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthorNewsViewModel @Inject constructor(
    private val loadAuthorNewsUseCase: LoadAuthorNewsUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val favoriteAuthorCheckUseCase: FavoriteAuthorCheckUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsState>(NewsState.Loading)
    val uiState: StateFlow<NewsState> get() = _uiState

    private var _user = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val user: StateFlow<AuthState> get() = _user.asStateFlow()

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

    fun preloadAuthorData(author: String){
        viewModelScope.launch {
            favoriteAuthorCheckUseCase(author)
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect{ userFirebase ->
                if (userFirebase != null) _user.value = AuthState.LoggedIn(userFirebase)
                else _user.value = AuthState.LoggedOut
            }
        }
    }

    init {
        observeAuthState()
    }
}