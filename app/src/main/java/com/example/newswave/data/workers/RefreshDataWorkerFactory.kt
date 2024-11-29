package com.example.newswave.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.database.dbNews.NewsDb
import com.example.newswave.data.database.dbNews.UserPreferences
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.network.api.ApiFactory
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.domain.repository.RemoteDataSource
import javax.inject.Inject

class RefreshDataWorkerFactory @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val newsInfoDao: NewsDao,
    private val mapper: NewsMapper,
    private val userPreferences: UserPreferences
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return RefreshDataWorker(
            appContext,
            workerParameters,
            remoteDataSource,
            newsInfoDao,
            mapper,
            userPreferences
        )
    }
}