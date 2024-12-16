package com.example.newswave.data.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.newswave.R
import com.example.newswave.data.source.local.NewsDbModel
import com.example.newswave.domain.repository.LocalDataSource
import com.example.newswave.domain.repository.RemoteDataSource
import com.example.newswave.utils.DateUtils

/**
 * Использует WorkManager для выполнения задач в фоновом режиме
 * Выполняет:
 * - Загрузку новостей с удаленного API через RemoteDataSource
 * - Обновление локальной базы данных через LocalDataSource
 */
class RefreshDataWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : CoroutineWorker(context, workerParameters) {

    /**
     * Основной метод WorkManager, который выполняется при запуске Worker
     * Если операция успешна — возвращает Result.success(), иначе — Result.failure()
     */
    override suspend fun doWork(): Result {
        return try {
            loadData()
            Result.success()
        } catch (e: Exception) {
            val outPutData = workDataOf("error" to e.message)
            Result.failure(outPutData)
        }
    }

    // Загружает данные из удаленного источника и обновляет локальную базу данных
    private suspend fun loadData() {
        var date = DateUtils.formatCurrentDate()

        val newsList = try {
            var result: List<NewsDbModel> = emptyList()
            for (attempt in 1..2) {
                val news = remoteDataSource.fetchTopNews(date)
                if (news.isNotEmpty()) {
                    result = news
                    break
                }
                date = DateUtils.formatDateToYesterday()
            }
            if (result.isEmpty()) {
                throw Exception(context.getString(R.string.news_list_is_empty_or_invalid_parameters))
            }
            result
        } catch (e: Exception) {
            throw Exception(e.message.toString())
        }


        // Обновление локальной базы данных
        localDataSource.deleteAllNews()
        localDataSource.insertNews(newsList)
    }


    companion object {
        const val WORK_NAME = "refresh news"

        // Создает запрос для одноразового выполнения `WorkManager`.
        fun makeRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<RefreshDataWorker>().apply {
                setConstraints(makeConstraints())
            }.build()
        }

        // Настраивает ограничения для Worker.
        private fun makeConstraints(): Constraints {
            return Constraints.Builder()
                .build()
        }
    }
}
