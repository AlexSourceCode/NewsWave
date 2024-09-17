package com.example.newswave.di

import android.content.Context
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.fragments.TopNewsFragment
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)

    fun inject(fragment: TopNewsFragment)

    @Component.Factory
    interface ApplicationComponentFactory{

        fun create(@BindsInstance context: Context): ApplicationComponent
    }

}