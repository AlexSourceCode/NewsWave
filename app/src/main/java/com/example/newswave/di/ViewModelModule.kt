package com.example.newswave.di

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCase
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCaseFactory
import com.example.newswave.presentation.viewModels.AuthorNewsViewModel
import com.example.newswave.presentation.viewModels.NewsDetailsViewModel
import com.example.newswave.presentation.viewModels.SubscribedAuthorsViewModel
import com.example.newswave.presentation.viewModels.TopNewsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
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

}