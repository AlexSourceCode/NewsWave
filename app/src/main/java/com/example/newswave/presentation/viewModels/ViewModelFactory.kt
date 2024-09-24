package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
    private val viewModelProviders: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>,//мб другой провайдер использовать
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopNewsViewModel::class.java)) {
            return viewModelProviders[modelClass]?.get() as T
        }
        if (modelClass.isAssignableFrom(SubscribedAuthorsViewModel::class.java)){
            return viewModelProviders[modelClass]?.get() as T
        }
        if (modelClass.isAssignableFrom(NewsDetailsViewModel::class.java)){
            return viewModelProviders[modelClass]?.get() as T
        }
        if (modelClass.isAssignableFrom(AuthorNewsViewModel::class.java)){
            return viewModelProviders[modelClass]?.get() as T
        }
        throw RuntimeException("Unknown view model class $modelClass")
    }
}