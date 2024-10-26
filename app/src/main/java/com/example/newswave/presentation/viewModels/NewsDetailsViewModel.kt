package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.usecases.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.IsFavoriteAuthorUseCase
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
    private val isFavoriteAuthorUseCase: IsFavoriteAuthorUseCase
): ViewModel() {

    private val _stateAuthor = MutableStateFlow<Boolean?>(null)
    val stateAuthor: StateFlow<Boolean?> get() = _stateAuthor.asStateFlow()


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

    init {
        viewModelScope.launch {
            isFavoriteAuthorUseCase().collect{ state ->
                _stateAuthor.value = state
            }
        }
    }
}