package com.example.newswave.presentation.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelFactory @Inject constructor(
    private val viewModelProviders: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>,//мб другой провайдер использовать
    private val savedStateHandleProvider: Provider<SavedStateHandle>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopNewsViewModel::class.java)) {
            val handle = savedStateHandleProvider.get()
            return viewModelProviders[modelClass]?.get().apply {
                (this as TopNewsViewModel).savedStateHandle = handle
            } as T
        }
        return viewModelProviders[modelClass]?.get() as T
    }
}