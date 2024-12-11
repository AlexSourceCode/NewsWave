package com.example.newswave.presentation.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.R
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.presentation.state.AuthState
import com.example.newswave.presentation.state.NewsState
import com.example.newswave.domain.usecases.subscription.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.subscription.LoadAuthorNewsUseCase
import com.example.newswave.domain.usecases.user.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления логикой отображения новостей автора
 */
class AuthorNewsViewModel @Inject constructor(
    private val application: Application,
    private val loadAuthorNewsUseCase: LoadAuthorNewsUseCase, // UseCase для загрузки новостей автора
    private val observeAuthStateUseCase: ObserveAuthStateUseCase, // UseCase для наблюдения за состоянием аутентификации
    private val favoriteAuthorCheckUseCase: FavoriteAuthorCheckUseCase // UseCase для проверки избранного автора
) : ViewModel() {

    // Поток состояния UI, изначально в состоянии загрузки
    private val _uiState = MutableStateFlow<NewsState>(NewsState.Loading)
    val uiState: StateFlow<NewsState> get() = _uiState

    // Поток для хранения состояния авторизации пользователя
    private var _user = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val user: StateFlow<AuthState> get() = _user.asStateFlow()

    init {
        observeAuthState() // Запуск наблюдения за состоянием авторизации при создании ViewModel
    }

    // Обновляет данные новостей
    fun refreshAuthorNews(author: String) {
        viewModelScope.launch {
            try {
                loadAuthorNewsUseCase(author)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // Обрабатывает результат загрузки новостей и обновляет состояние UI
    private fun handleNewsResult(news: List<NewsItemEntity>) {
        _uiState.value = if (news.isEmpty()) {
            NewsState.Error(application.getString(R.string.no_internet_connection))
        } else {
            NewsState.Success(news)
        }
    }

    // Обрабатывает ошибки и обновляет состояние UI
    private fun handleError(e: Exception) {
        _uiState.value = NewsState.Error(e.message.toString())
    }

    // Загружает новости для автора в виде потока
    // Использует collect для обновления состояния при каждом новом элементе
    fun loadAuthorNews(author: String) {
        viewModelScope.launch {
            try {
                loadAuthorNewsUseCase(author).collect { news ->
                    handleNewsResult(news)
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // Предварительная загрузка данных об авторе
    fun preloadAuthorData(author: String) {
        viewModelScope.launch {
            favoriteAuthorCheckUseCase(author)
        }
    }

    // Наблюдает за изменениями состояния аутентификации пользователя
    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { userFirebase ->
                _user.value = userFirebase?.let { AuthState.LoggedIn(it) } ?: AuthState.LoggedOut
            }
        }
    }
}