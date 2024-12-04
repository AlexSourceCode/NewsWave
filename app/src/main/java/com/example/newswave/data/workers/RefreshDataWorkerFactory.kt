package com.example.newswave.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.newswave.domain.repository.LocalDataSource
import com.example.newswave.domain.repository.RemoteDataSource
import javax.inject.Inject

/**
 * Фабрика для создания экземпляров RefreshDataWorker
 * Используется для внедрения зависимостей через Dagger
 */
class RefreshDataWorkerFactory @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : WorkerFactory() {

    // Создает экземпляр RefreshDataWorker с параметрами
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return RefreshDataWorker(
            appContext,
            workerParameters,
            remoteDataSource,
            localDataSource,
        )
    }

}