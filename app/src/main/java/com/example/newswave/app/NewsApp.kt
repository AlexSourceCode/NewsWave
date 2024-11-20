package com.example.newswave.app

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.example.newswave.data.database.dbNews.UserPreferences
import com.example.newswave.data.workers.RefreshDataWorkerFactory
import com.example.newswave.di.DaggerApplicationComponent
import javax.inject.Inject

class NewsApp: Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: RefreshDataWorkerFactory

    val component by lazy {
        DaggerApplicationComponent.factory()
            .create(
                applicationContext,
                getSharedPreferences("news_by_search", Context.MODE_PRIVATE)
                )
    }

    override fun onCreate() {
        component.inject(this)
        super.onCreate()

        val userPreferences = UserPreferences(this)
        userPreferences.initializeDefaultSettings()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


}