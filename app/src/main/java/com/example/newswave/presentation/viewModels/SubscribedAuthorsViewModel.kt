package com.example.newswave.presentation.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.usecases.GetAuthorListUseCase
import com.example.newswave.domain.usecases.SubscribeOnAuthorUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscribedAuthorsViewModel @Inject constructor(
    private val getAuthorListUseCase: GetAuthorListUseCase,
    private val subscribeOnAuthorUseCase: SubscribeOnAuthorUseCase
) : ViewModel() {


    private val _authorList = MutableLiveData<List<AuthorItemEntity>>()
    val authorList: LiveData<List<AuthorItemEntity>>
        get() = _authorList

    init {
        viewModelScope.launch {
//            subscribeOnSourceUseCase(AuthorDbModel(author = "Contributor"))
            getAuthorListUseCase().collect { authors ->
                _authorList.postValue(authors)
            }
        }
    }


}