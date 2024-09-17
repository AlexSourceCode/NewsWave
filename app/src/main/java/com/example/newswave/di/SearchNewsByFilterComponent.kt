package com.example.newswave.di

import com.example.newswave.domain.usecases.SearchNewsByFilterUseCase
import com.example.newswave.presentation.fragments.TopNewsFragment
import dagger.BindsInstance
import dagger.Subcomponent
import dagger.assisted.Assisted


//@Subcomponent
////    (
////    modules = [ViewModelModule::class]
////)
//interface SearchNewsByFilterComponent {
//
//    fun searchNewsByFilterUseCase(): SearchNewsByFilterUseCase
//
//    @Subcomponent.Factory
//    interface Factory{
//
//        fun create(
//            @BindsInstance @Assisted("filter")filterParameter: String,
//            @BindsInstance @Assisted("value")valueParameter: String
//        ): SearchNewsByFilterComponent
//    }
//    fun inject(fragment: TopNewsFragment)


//}