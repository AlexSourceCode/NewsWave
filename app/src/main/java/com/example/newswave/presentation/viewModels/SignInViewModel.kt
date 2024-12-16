package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.network.model.ErrorType
import com.example.newswave.domain.usecases.user.FetchAuthErrorUseCase
import com.example.newswave.domain.usecases.user.ObserveAuthStateUseCase
import com.example.newswave.domain.usecases.user.SignInByEmailUseCase
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
 * ViewModel для управления логикой авторизации.
 */
class SignInViewModel @Inject constructor(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val signInByEmailUseCase: SignInByEmailUseCase,
    private val fetchAuthErrorUseCase: FetchAuthErrorUseCase
): ViewModel() {

    // Поток для отправки ошибок
    private var _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> get() = _error.asSharedFlow()

    // Состояние авторизованного пользователя
    private var _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user.asStateFlow()

    init {
        observeAuthState()
        observeAuthErrors()
    }

    // Вход пользователя по email и паролю
    fun signIn(email:String, password: String){
        viewModelScope.launch {
            signInByEmailUseCase(email, password)
        }
    }

    // Подписка на изменения состояния авторизации
    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect {
                _user.value = it
            }
        }
    }

    // Подписка на получение ошибок авторизации
    private fun observeAuthErrors(){
        viewModelScope.launch {
            fetchAuthErrorUseCase(ErrorType.SIGN_IN).collect{
                _error.emit(it)
            }
        }
    }
}