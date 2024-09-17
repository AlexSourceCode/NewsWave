package com.example.newswave.presentation

import android.app.Application
import androidx.work.Configuration
import com.example.newswave.data.database.dbNews.NewsDb
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.network.api.ApiFactory
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.workers.RefreshDataWorkerFactory
import com.example.newswave.di.DaggerApplicationComponent
import javax.inject.Inject

class NewsApp: Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: RefreshDataWorkerFactory

    val component by lazy {
        DaggerApplicationComponent.factory()
            .create(applicationContext)
    }

    override fun onCreate() {
        component.inject(this)
        super.onCreate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


}