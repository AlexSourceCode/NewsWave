package com.example.newswave.presentation.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

/**
 * Общая фабрика для создания ViewModel, предоставляющая зависимость SavedStateHandle.
 * Позволяет передавать SavedStateHandle в нужные ViewModel
 */
class ViewModelFactory @Inject constructor(
    private val viewModelProviders: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>,//мб другой провайдер использовать
    private val savedStateHandleProvider: Provider<SavedStateHandle>
) : ViewModelProvider.Factory {

    //Создает экземпляр ViewModel. Если требуется SavedStateHandle, он будет инжектирован
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