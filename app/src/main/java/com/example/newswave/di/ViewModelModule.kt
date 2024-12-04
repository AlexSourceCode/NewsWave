package com.example.newswave.di

import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.newswave.presentation.viewModels.AuthorNewsViewModel
import com.example.newswave.presentation.viewModels.ForgotPasswordViewModel
import com.example.newswave.presentation.viewModels.SignInViewModel
import com.example.newswave.presentation.viewModels.NewsDetailsViewModel
import com.example.newswave.presentation.viewModels.RegistrationViewModel
import com.example.newswave.presentation.viewModels.SessionViewModel
import com.example.newswave.presentation.viewModels.SettingsViewModel
import com.example.newswave.presentation.viewModels.SubscribedAuthorsViewModel
import com.example.newswave.presentation.viewModels.TopNewsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

//Модуль со всеми вьюмоделями, для того чтобы все они инжектились в вьюмодель фектори
@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(TopNewsViewModel::class)
    fun bindTopNewsViewModel(viewModel: TopNewsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SubscribedAuthorsViewModel::class)
    fun bindSubscribedAuthorsViewModel(viewModel: SubscribedAuthorsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewsDetailsViewModel::class)
    fun bindNewsDetailsViewModel(viewModel: NewsDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthorNewsViewModel::class)
    fun bindAuthorNewsViewModel(viewModel: AuthorNewsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    fun bindSignInViewModel(viewmodel: SignInViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegistrationViewModel::class)
    fun bindRegistrationViewModel(viewModel: RegistrationViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ForgotPasswordViewModel::class)
    fun bindForgotPasswordViewModel(viewModel: ForgotPasswordViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun bindSettingsViewModel(viewModel: SettingsViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SessionViewModel::class)
    fun bindSessionViewModel(viewModel: SessionViewModel):ViewModel


    @Binds
    abstract fun bindViewModelFactory(factory: SavedStateViewModelFactory): ViewModelProvider.Factory

}