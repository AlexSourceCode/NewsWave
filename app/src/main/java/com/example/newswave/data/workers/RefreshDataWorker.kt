package com.example.newswave.data.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.newswave.R
import com.example.newswave.data.dataSource.local.NewsDbModel
import com.example.newswave.domain.repository.LocalDataSource
import com.example.newswave.domain.repository.RemoteDataSource
import com.example.newswave.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RefreshDataWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : CoroutineWorker(context, workerParameters) {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    override suspend fun doWork(): Result {
        return try {
            loadData()
            Result.success()
        } catch (e: Exception) {
            val outPutData = workDataOf("error" to e.message)
            Result.failure(outPutData)
        }
    }


    private suspend fun loadData() {
        Log.d("CheckErrorMessage", "execute loadData from dowork")
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


        ioScope.launch {
            localDataSource.deleteAllNews()
            localDataSource.insertNews(newsList)
        }
    }


    companion object {
        const val WORK_NAME = "refresh news"

        fun makeRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<RefreshDataWorker>().apply {
                setConstraints(makeConstraints())
            }.build()
        }

        private fun makeConstraints(): Constraints {
            return Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED) // как сделать так, чтобы запросы шли только если есть интернет
                .build()
        }
    }
}
