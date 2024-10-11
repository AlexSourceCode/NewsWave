package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.usecases.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.SubscribeToAuthorUseCase
import com.example.newswave.domain.usecases.UnsubscribeFromAuthorUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewsDetailsViewModel @Inject constructor(
    private val favoriteAuthorCheckUseCase: FavoriteAuthorCheckUseCase,
    private val subscribeToAuthorUseCase: SubscribeToAuthorUseCase,
    private val unsubscribeFromAuthorUseCase: UnsubscribeFromAuthorUseCase
): ViewModel() {

    private val _stateAuthor = MutableStateFlow(true)//под вопросом использование Flow
    val stateAuthor: StateFlow<Boolean> get() = _stateAuthor.asStateFlow()


    fun checkAuthorInRepository(author: String){
        viewModelScope.launch {
            val result = favoriteAuthorCheckUseCase(author)
            _stateAuthor.value = result
        }
    }

    fun subscribeOnAuthor(author: String){
        viewModelScope.launch {
            subscribeToAuthorUseCase(author)
            checkAuthorInRepository(author)
        }
    }

    fun unsubscribeFromAuthor(author: String){
        viewModelScope.launch {
            unsubscribeFromAuthorUseCase(author)
            checkAuthorInRepository(author)
        }
    }
}