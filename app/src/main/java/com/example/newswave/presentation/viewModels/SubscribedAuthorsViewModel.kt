package com.example.newswave.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.model.AuthState
import com.example.newswave.domain.model.AuthorState
import com.example.newswave.domain.usecases.subscription.GetAuthorListUseCase
import com.example.newswave.domain.usecases.user.ObserveAuthStateUseCase
import com.example.newswave.domain.usecases.subscription.ShowAuthorsListUseCase
import com.example.newswave.domain.usecases.subscription.UnsubscribeFromAuthorUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class SubscribedAuthorsViewModel @Inject constructor(
    private val getAuthorListUseCase: GetAuthorListUseCase,
    private val unsubscribeFromAuthorUseCase: UnsubscribeFromAuthorUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    showAuthorsListUseCase: ShowAuthorsListUseCase
) : ViewModel() {


    private var _user = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val user: StateFlow<AuthState> get() = _user.asStateFlow()

    private val _uiState = MutableStateFlow<AuthorState>(AuthorState.Loading)
    val uiState: StateFlow<AuthorState> get() = _uiState.asStateFlow()


    fun unsubscribeFromAuthor(author: String){
        viewModelScope.launch {
            unsubscribeFromAuthorUseCase(author)
        }
    }

    fun retryFetchAuthors() {
        Log.d("SubscribedAuthorsViewModel", "retryFetchAuthors")
        getAuthorsList()
    }

    private fun getAuthorsList() {
        Log.d("SubscribedAuthorsViewModel", "getAuthorsList")
        viewModelScope.launch {
            try {
                Log.d("SubscribedAuthorsViewModel", "before collectLatest")
                getAuthorListUseCase().collect { authors ->
                    Log.d("SubscribedAuthorsViewModel", "after collectLatest")
                    _uiState.value = AuthorState.Success(authors)
                }
            } catch (e: Exception) {
                _uiState.value = AuthorState.Error(e.message.toString())
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { userFirebase ->
                if (userFirebase != null) _user.value = AuthState.LoggedIn(userFirebase)
                else _user.value = AuthState.LoggedOut
            }
        }
    }


    init {
        showAuthorsListUseCase()
        getAuthorsList()
        observeAuthState()
    }

}

