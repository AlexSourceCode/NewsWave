package com.example.newswave.app

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.example.newswave.data.source.local.UserPreferences
import com.example.newswave.data.worker.RefreshDataWorkerFactory
import com.example.newswave.di.DaggerApplicationComponent
import com.example.newswave.domain.repositories.UserRepository
import com.example.newswave.utils.LanguageUtils
import javax.inject.Inject

/**
 * Главный класс приложения
 * Управляет внедрением зависимостей и настройкой WorkManager
 */
class NewsApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: RefreshDataWorkerFactory

    @Inject
    lateinit var userRepository: UserRepository

    // Ленивая инициализация компонента Dagger для внедрения зависимостей
    val component by lazy {
        DaggerApplicationComponent.factory()
            .create(
                applicationContext,
                getSharedPreferences("news_by_search", Context.MODE_PRIVATE)
            )
    }

    // Инициализация приложения, включая настройки пользователя
    override fun onCreate() {
        component.inject(this)
        super.onCreate()
        LanguageUtils.initialize(this)

        val userPreferences = UserPreferences(this)
        userPreferences.initializeDefaultSettings()
    }

    // Конфигурация WorkManager с кастомной фабрикой задач
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}