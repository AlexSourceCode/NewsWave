package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.network.model.ErrorType
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

class RegistrationViewModel @Inject constructor(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val signUpByEmailUseCase: SignUpByEmailUseCase,
    private val fetchAuthErrorUseCase: FetchAuthErrorUseCase

) : ViewModel() {

    private var _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user.asStateFlow()

    private var _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> get() = _error.asSharedFlow()

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect {
                _user.value = it
            }
        }
    }

    fun signUp(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        signUpByEmailUseCase(username, email, password, firstName,lastName)
    }

    init {
        observeAuthState()
        viewModelScope.launch {
            fetchAuthErrorUseCase(ErrorType.SIGN_UP).collect{
                _error.emit(it)
            }
        }
    }
}