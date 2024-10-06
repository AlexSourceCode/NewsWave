package com.example.newswave.di

import android.content.Context
import android.content.SharedPreferences
import com.example.newswave.presentation.MainActivity
import com.example.newswave.app.NewsApp
import com.example.newswave.presentation.fragments.AuthorNewsFragment
import com.example.newswave.presentation.fragments.NewsDetailsFragment
import com.example.newswave.presentation.fragments.SubscribedAuthorsFragment
import com.example.newswave.presentation.fragments.TopNewsFragment
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)

    fun inject(fragment: TopNewsFragment)

    fun inject(fragment: SubscribedAuthorsFragment)

    fun inject(fragment: NewsDetailsFragment)

    fun inject(fragment: AuthorNewsFragment)

    fun inject(application: NewsApp)

    @Component.Factory
    interface ApplicationComponentFactory{

        fun create(
            @BindsInstance context: Context,
            @BindsInstance sharedPreferences: SharedPreferences
            ): ApplicationComponent
    }

}