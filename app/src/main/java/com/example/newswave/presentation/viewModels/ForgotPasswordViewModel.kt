package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.network.model.ErrorType
import com.example.newswave.domain.usecases.user.FetchAuthErrorUseCase
import com.example.newswave.domain.usecases.user.FetchIsSuccessAuthUseCase
import com.example.newswave.domain.usecases.user.ResetPasswordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ForgotPasswordViewModel обрабатывает логику сброса пароля и предоставляет потоки для статуса выполнения и ошибок
 */
class ForgotPasswordViewModel @Inject constructor(
    private val fetchAuthErrorUseCase: FetchAuthErrorUseCase,
    private val fetchIsSuccessAuthUseCase: FetchIsSuccessAuthUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel() {

    // Потоки для успешного выполнения и ошибок
    private var _isSuccess = MutableSharedFlow<Boolean>()
    val isSuccess: SharedFlow<Boolean> get() = _isSuccess.asSharedFlow()

    private var _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> get() = _error.asSharedFlow()

    init {
        subscribeToAuthError()
        subscribeToSuccessStatus()
    }

    // Сброс пароля
    fun resetPassword(email: String) {
        resetPasswordUseCase(email)
    }

    // Подписка на поток успешного выполнения операции.
    private fun subscribeToSuccessStatus() {
        viewModelScope.launch {
            fetchIsSuccessAuthUseCase().collect { success ->
                _isSuccess.emit(success)
            }
        }
    }

    // Подписка на поток ошибок.
    private fun subscribeToAuthError() {
        viewModelScope.launch {
            fetchAuthErrorUseCase(ErrorType.FORGOT_PASSWORD).collect { errorMessage ->
                _error.emit(errorMessage)
            }
        }
    }
}