package com.example.newswave.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.database.dbAuthors.AuthorDb
import com.example.newswave.data.database.dbAuthors.AuthorDbModel
import com.example.newswave.domain.usecases.FavoriteAuthorCheckUseCase
import com.example.newswave.domain.usecases.SubscribeOnAuthorUseCase
import com.example.newswave.domain.usecases.UnsubscribeFromAuthorUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NewsDetailsViewModel @Inject constructor(
    private val favoriteAuthorCheckUseCase: FavoriteAuthorCheckUseCase,
    private val subscribeOnAuthorUseCase: SubscribeOnAuthorUseCase,
    private val unsubscribeFromAuthorUseCase: UnsubscribeFromAuthorUseCase
): ViewModel() {

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> get() = _isFavorite

    fun checkAuthorInRepository(author: String){
        viewModelScope.launch {
            val result = favoriteAuthorCheckUseCase(author)
            _isFavorite.value = result
        }
    }

    fun subscribeOnAuthor(author: String){
        viewModelScope.launch {
            subscribeOnAuthorUseCase(author)
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