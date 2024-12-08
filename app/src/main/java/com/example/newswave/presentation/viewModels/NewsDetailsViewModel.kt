package com.example.newswave.presentation.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.presentation.state.AuthState
import com.example.newswave.domain.usecases.subscription.ClearStateUseCase
import com.example.newswave.domain.usecases.subscription.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.subscription.IsFavoriteAuthorUseCase
import com.example.newswave.domain.usecases.user.ObserveAuthStateUseCase
import com.example.newswave.domain.usecases.subscription.SubscribeToAuthorUseCase
import com.example.newswave.domain.usecases.subscription.UnsubscribeFromAuthorUseCase
import com.example.newswave.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsDetailsViewModel @Inject constructor(
    private val favoriteAuthorCheckUseCase: FavoriteAuthorCheckUseCase,
    private val subscribeToAuthorUseCase: SubscribeToAuthorUseCase,
    private val unsubscribeFromAuthorUseCase: UnsubscribeFromAuthorUseCase,
    private val isFavoriteAuthorUseCase: IsFavoriteAuthorUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val clearStateUseCase: ClearStateUseCase
) : ViewModel() {

    private var _stateAuthor = MutableStateFlow<Boolean?>(null)
    val stateAuthor: StateFlow<Boolean?> get() = _stateAuthor.asStateFlow()


    private var _user = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val user: StateFlow<AuthState> get() = _user.asStateFlow()


    fun checkAuthorInRepository(author: String) {
        viewModelScope.launch {
            favoriteAuthorCheckUseCase(author)
        }
    }

    fun subscribeOnAuthor(author: String) {
        viewModelScope.launch {
            subscribeToAuthorUseCase(author)
        }
    }

    fun unsubscribeFromAuthor(author: String) {
        viewModelScope.launch {
            unsubscribeFromAuthorUseCase(author)
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { userFirebase ->
                if (userFirebase != null) {
                    Log.d("CheckStateExecute", "observeAuthStateIf")
                    _user.value = AuthState.LoggedIn(userFirebase)
                    observeFavoriteAuthor()
                } else {
                    Log.d("CheckStateExecute", "observeAuthStateElse")
                    _user.value = AuthState.LoggedOut
                }
            }
        }
    }

    private fun observeFavoriteAuthor() {
        viewModelScope.launch {
            isFavoriteAuthorUseCase().collectLatest { state ->
                Log.d("CheckStateExecute", "observeFavoriteAuthor $state")
                if (state == null) return@collectLatest
                _stateAuthor.value = state
            }
        }
    }

    fun isInternetConnection(context: Context): Boolean {
        return NetworkUtils.isNetworkAvailable(context)
    }

    fun clearState() {
        clearStateUseCase()
    }

    init {
        observeAuthState()
    }
}