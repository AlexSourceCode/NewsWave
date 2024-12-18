package com.example.newswave.di

import android.content.Context
import android.content.SharedPreferences
import com.example.newswave.app.NewsApp
import com.example.newswave.presentation.activity.MainActivity
import com.example.newswave.presentation.fragments.AuthorNewsFragment
import com.example.newswave.presentation.fragments.ForgotPasswordFragment
import com.example.newswave.presentation.fragments.NewsDetailsFragment
import com.example.newswave.presentation.fragments.RegistrationFragment
import com.example.newswave.presentation.fragments.SettingsFragment
import com.example.newswave.presentation.fragments.SignInFragment
import com.example.newswave.presentation.fragments.SubscribedAuthorsFragment
import com.example.newswave.presentation.fragments.TopNewsFragment
import dagger.BindsInstance
import dagger.Component

/**
 * Компонент Dagger для внедрения зависимостей на уровне приложения
 * Включает модули DataModule и ViewModelModule
 * Предоставляет зависимости для активностей, фрагментов и приложения
 */
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
    fun inject(fragment: SignInFragment)
    fun inject(fragment: RegistrationFragment)
    fun inject(fragment: ForgotPasswordFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(application: NewsApp)


    @Component.Factory
    interface ApplicationComponentFactory{

        // Создает экземпляр ApplicationComponent с параметрами
        fun create(
            @BindsInstance context: Context,
            @BindsInstance sharedPreferences: SharedPreferences
            ): ApplicationComponent
    }

}