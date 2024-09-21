package com.example.newswave.di

import android.content.Context
import android.content.SharedPreferences
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.NewsApp
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

    fun inject(application: NewsApp)

    @Component.Factory
    interface ApplicationComponentFactory{

        fun create(
            @BindsInstance context: Context,
            @BindsInstance sharedPreferences: SharedPreferences
            ): ApplicationComponent
    }

}