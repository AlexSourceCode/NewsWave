package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.usecases.subscription.GetAuthorListUseCase
import com.example.newswave.domain.usecases.subscription.UnsubscribeFromAuthorUseCase
import com.example.newswave.domain.usecases.user.ObserveAuthStateUseCase
import com.example.newswave.presentation.state.AuthState
import com.example.newswave.presentation.state.AuthorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления состоянием экрана подписанных авторов
 */
class SubscribedAuthorsViewModel @Inject constructor(
    private val getAuthorListUseCase: GetAuthorListUseCase,
    private val unsubscribeFromAuthorUseCase: UnsubscribeFromAuthorUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
) : ViewModel() {

    // Состояние авторизации пользователя
    private var _user = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val user: StateFlow<AuthState> get() = _user.asStateFlow()

    // Состояние UI
    private val _uiState = MutableStateFlow<AuthorState>(AuthorState.Loading)
    val uiState: StateFlow<AuthorState> get() = _uiState.asStateFlow()

    init {
        getAuthorsList()
        observeAuthState()
    }

    // Отписка от автора
    fun unsubscribeFromAuthor(author: String){
        viewModelScope.launch {
            unsubscribeFromAuthorUseCase(author)
        }
    }

    // Повторная попытка загрузки списка авторов
    fun retryFetchAuthors() {
        getAuthorsList()
    }

    // Получение списка авторов
    private fun getAuthorsList() {
        viewModelScope.launch {
            try {
                getAuthorListUseCase().collect { authors ->
                    _uiState.value = AuthorState.Success(authors)
                }
            } catch (e: Exception) {
                _uiState.value = AuthorState.Error(e.message.toString())
            }
        }
    }

    // Метод для наблюдения за состоянием аутентификации
    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { userFirebase ->
                if (userFirebase != null) _user.value = AuthState.LoggedIn(userFirebase)
                else _user.value = AuthState.LoggedOut
            }
        }
    }
}

