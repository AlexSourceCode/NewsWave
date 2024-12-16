package com.example.newswave.presentation.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.usecases.subscription.ClearStateUseCase
import com.example.newswave.domain.usecases.subscription.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.subscription.IsFavoriteAuthorUseCase
import com.example.newswave.domain.usecases.subscription.SubscribeToAuthorUseCase
import com.example.newswave.domain.usecases.subscription.UnsubscribeFromAuthorUseCase
import com.example.newswave.domain.usecases.user.ObserveAuthStateUseCase
import com.example.newswave.presentation.state.AuthState
import com.example.newswave.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *  ViewModel для экрана новостей, который управляет состояниями подписки на автора
 *  и авторизации пользователя
 */
class NewsDetailsViewModel @Inject constructor(
    private val favoriteAuthorCheckUseCase: FavoriteAuthorCheckUseCase,
    private val subscribeToAuthorUseCase: SubscribeToAuthorUseCase,
    private val unsubscribeFromAuthorUseCase: UnsubscribeFromAuthorUseCase,
    private val isFavoriteAuthorUseCase: IsFavoriteAuthorUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val clearStateUseCase: ClearStateUseCase
) : ViewModel() {

    // Состояние подписки на автора
    private var _authorState = MutableStateFlow<Boolean?>(null)
    val authorState: StateFlow<Boolean?> get() = _authorState.asStateFlow()

    // Состояние пользователя
    private var _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> get() = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    // Проверяет, добавлен ли автор в избранное
    fun checkAuthorInRepository(author: String) {
        viewModelScope.launch {
            favoriteAuthorCheckUseCase(author)
        }
    }

    // Подписка пользователя на автора
    fun subscribeOnAuthor(author: String) {
        viewModelScope.launch {
            subscribeToAuthorUseCase(author)
        }
    }

    // Отписка пользователя от автора
    fun unsubscribeFromAuthor(author: String) {
        viewModelScope.launch {
            unsubscribeFromAuthorUseCase(author)
        }
    }

    // Наблюдает за изменениями в состоянии авторизации пользователя
    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase()
                .collect { userFirebase ->
                    if (userFirebase != null) {
                        _authState.value = AuthState.LoggedIn(userFirebase)
                        observeFavoriteAuthor()
                    } else {
                        _authState.value = AuthState.LoggedOut
                    }
                }
        }
    }

    // Наблюдает за состоянием избранного автора
    private fun observeFavoriteAuthor() {
        viewModelScope.launch {
            isFavoriteAuthorUseCase()
                .filterNotNull()
                .collectLatest { state ->
                    _authorState.value = state
                }
        }
    }

    // Проверяет наличие подключения к интернету
    fun isInternetConnection(context: Context): Boolean {
        return NetworkUtils.isNetworkAvailable(context)
    }

    // Очищает состояния ViewModel
    fun clearState() {
        clearStateUseCase()
    }
}