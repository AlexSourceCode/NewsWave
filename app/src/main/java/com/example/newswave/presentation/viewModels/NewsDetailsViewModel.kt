package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.model.AuthState
import com.example.newswave.domain.usecases.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.IsFavoriteAuthorUseCase
import com.example.newswave.domain.usecases.ObserveAuthStateUseCase
import com.example.newswave.domain.usecases.SubscribeToAuthorUseCase
import com.example.newswave.domain.usecases.UnsubscribeFromAuthorUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsDetailsViewModel @Inject constructor(
    private val favoriteAuthorCheckUseCase: FavoriteAuthorCheckUseCase,
    private val subscribeToAuthorUseCase: SubscribeToAuthorUseCase,
    private val unsubscribeFromAuthorUseCase: UnsubscribeFromAuthorUseCase,
    private val isFavoriteAuthorUseCase: IsFavoriteAuthorUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    ): ViewModel() {

    private val _stateAuthor = MutableStateFlow<Boolean?>(null)
    val stateAuthor: StateFlow<Boolean?> get() = _stateAuthor.asStateFlow()

    private var _user = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val user: StateFlow<AuthState> get() = _user.asStateFlow()


    fun checkAuthorInRepository(author: String){
        viewModelScope.launch {
            favoriteAuthorCheckUseCase(author)
        }
    }

    fun subscribeOnAuthor(author: String){
        viewModelScope.launch {
            subscribeToAuthorUseCase(author)
        }
    }

    fun unsubscribeFromAuthor(author: String){
        viewModelScope.launch {
            unsubscribeFromAuthorUseCase(author)
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { userFirebase ->
                if (userFirebase != null){
                    _user.value = AuthState.LoggedIn(userFirebase)
                } else{
                    _user.value = AuthState.LoggedOut
                    _stateAuthor.value = false
                }
            }
        }
    }

    private fun observeFavoriteAuthor(){
        viewModelScope.launch {
            isFavoriteAuthorUseCase().collect { state ->
                _stateAuthor.value = state
            }
        }
    }

    init {
        observeAuthState()
        observeFavoriteAuthor()
    }
}