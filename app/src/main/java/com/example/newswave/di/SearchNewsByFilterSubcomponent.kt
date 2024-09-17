package com.example.newswave.di

import com.example.newswave.domain.usecases.SearchNewsByFilterUseCase
import com.example.newswave.presentation.fragments.TopNewsFragment
import dagger.BindsInstance
import dagger.Subcomponent


@Subcomponent(
    modules = [ViewModelModule::class]
)
interface SearchNewsByFilterSubcomponent {

    @Subcomponent.Factory
    interface Factory{

        fun create(
            @BindsInstance filterParameter: String,
            @BindsInstance valueParameter: String
        ): SearchNewsByFilterSubcomponent
    }
    fun searchNewsByFilterUseCase(): SearchNewsByFilterUseCase
    fun inject(fragment: TopNewsFragment)


}