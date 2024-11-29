package com.example.newswave.presentation.viewModels



import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Inject
import javax.inject.Provider


class SavedStateViewModelFactory @Inject constructor(
    private val viewModelProviders: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        val provider = viewModelProviders[modelClass]
            ?: throw IllegalArgumentException("Unknown ViewModel class $modelClass")

        val viewModel = provider.get() as T

        // Инжектировать SavedStateHandle в конкретные ViewModel
        if (viewModel is TopNewsViewModel) {
            viewModel.savedStateHandle = handle
        }
        // Добавь другие ViewModel здесь по необходимости

        return viewModel
    }
}
