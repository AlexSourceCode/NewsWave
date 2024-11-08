package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.database.dbNews.UserPreferences
import com.example.newswave.domain.entity.UserEntity
import com.example.newswave.domain.usecases.FetchErrorAuthUseCase
import com.example.newswave.domain.usecases.ObserveAuthStateUseCase
import com.example.newswave.domain.usecases.SignUpByEmailUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    private val fetchErrorAuthUseCase: FetchErrorAuthUseCase

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
            fetchErrorAuthUseCase().collect{
                _error.emit(it)
            }
        }
    }
}