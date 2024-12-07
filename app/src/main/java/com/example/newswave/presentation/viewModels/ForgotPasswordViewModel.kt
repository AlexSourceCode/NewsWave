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

class ForgotPasswordViewModel @Inject constructor(
    private val fetchAuthErrorUseCase: FetchAuthErrorUseCase,
    private val fetchIsSuccessAuthUseCase: FetchIsSuccessAuthUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel() {


    private var _isSuccess = MutableSharedFlow<Boolean>()
    val isSuccess: SharedFlow<Boolean> get() = _isSuccess.asSharedFlow()

    private var _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> get() = _error.asSharedFlow()



    fun resetPassword(email: String) {
        resetPasswordUseCase(email)
    }

    init {
        viewModelScope.launch {
            fetchAuthErrorUseCase(ErrorType.FORGOT_PASSWORD).collect{
                _error.emit(it)
            }
        }
        viewModelScope.launch {
            fetchIsSuccessAuthUseCase().collect{
                _isSuccess.emit(it)
            }
        }
    }

}