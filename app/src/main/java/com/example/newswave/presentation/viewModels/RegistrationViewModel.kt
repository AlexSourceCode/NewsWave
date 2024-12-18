package com.example.newswave.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.network.models.ErrorType
import com.example.newswave.domain.usecases.user.FetchAuthErrorUseCase
import com.example.newswave.domain.usecases.user.ObserveAuthStateUseCase
import com.example.newswave.domain.usecases.user.SignUpByEmailUseCase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * RegistrationViewModel обрабатывает логику регистрации пользователя
 */
class RegistrationViewModel @Inject constructor(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val signUpByEmailUseCase: SignUpByEmailUseCase,
    private val fetchAuthErrorUseCase: FetchAuthErrorUseCase

) : ViewModel() {

    // Поток текущего пользователя
    private var _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user.asStateFlow()

    // Поток ошибок
    private var _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> get() = _error.asSharedFlow()

    init {
        observeAuthState()
        observeErrors()
    }

    // Регистрация нового пользователя
    fun signUp(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        viewModelScope.launch {
            signUpByEmailUseCase(username, email, password, firstName,lastName)
        }
    }

    // Наблюдение за состоянием авторизации
    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect {
                _user.value = it
            }
        }
    }

    // Наблюдение за ошибками
    private fun observeErrors() {
        viewModelScope.launch {
            fetchAuthErrorUseCase(ErrorType.SIGN_UP).collect { errorMessage ->
                _error.emit(errorMessage)
            }
        }
    }
}