package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.model.AuthorState
import com.example.newswave.domain.usecases.GetAuthorListUseCase
import com.example.newswave.domain.usecases.UnsubscribeFromAuthorUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.newswave.domain.model.NewsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class SubscribedAuthorsViewModel @Inject constructor(
    private val getAuthorListUseCase: GetAuthorListUseCase,
    private val unsubscribeFromAuthorUseCase: UnsubscribeFromAuthorUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthorState>(AuthorState.Loading)
    val uiState: StateFlow<AuthorState> get() = _uiState.asStateFlow()



    fun unsubscribeFromAuthor(author: String){
        viewModelScope.launch {
            unsubscribeFromAuthorUseCase(author)
        }
    }


    init {
        viewModelScope.launch {
            getAuthorListUseCase().collect { authors ->
                _uiState.value = (AuthorState.Success(authors))
            }
        }
    }

}

