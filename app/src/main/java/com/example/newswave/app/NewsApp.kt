package com.example.newswave.app

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.example.newswave.data.dataSource.local.UserPreferences
import com.example.newswave.data.workers.RefreshDataWorkerFactory
import com.example.newswave.di.DaggerApplicationComponent
import com.example.newswave.domain.repository.UserRepository
import com.example.newswave.utils.LanguageUtils
import javax.inject.Inject

/**
 * Главный класс приложения.
 * Управляет внедрением зависимостей и настройкой WorkManager.
 */
class NewsApp : Application(), Configuration.Provider {

    // Фабрика для создания рабочих задач (WorkManager).
    @Inject
    lateinit var workerFactory: RefreshDataWorkerFactory

    // Репозиторий пользователя.
    @Inject
    lateinit var userRepository: UserRepository

    // Ленивая инициализация компонента Dagger для внедрения зависимостей.
    val component by lazy {
        DaggerApplicationComponent.factory()
            .create(
                applicationContext,
                getSharedPreferences("news_by_search", Context.MODE_PRIVATE)
            )
    }

    // Инициализация приложения, включая настройки пользователя.
    override fun onCreate() {
        component.inject(this)
        super.onCreate()
        LanguageUtils.initialize(this)

        val userPreferences = UserPreferences(this)
        userPreferences.initializeDefaultSettings()
    }

    // Конфигурация WorkManager с кастомной фабрикой задач.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}