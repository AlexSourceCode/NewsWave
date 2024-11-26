package com.example.newswave.data.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.database.dbNews.NewsDb
import com.example.newswave.data.database.dbNews.UserPreferences
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.mapper.flattenToList
import com.example.newswave.data.network.api.ApiFactory
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.network.model.TopNewsResponseDto
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.utils.DateUtils
import com.example.newswave.utils.NetworkUtils.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch

class RefreshDataWorker(
    private val context: Context,
    private var workerParameters: WorkerParameters,
    private val apiService: ApiService,
    private val newsInfoDao: NewsDao,
    private val mapper: NewsMapper,
    private val userPreferences: UserPreferences
) : CoroutineWorker(context, workerParameters) {


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
        val country = userPreferences.getSourceCountry()
        val language = userPreferences.getContentLanguage()
        Log.d("loadDataStateArgs", country)
        Log.d("loadDataStateArgs", language)


        val jsonContainer: Flow<TopNewsResponseDto> = flow {
            for (attempt in 1..2) {
                val response = apiService.getListTopNews(
                    sourceCountry = country,
                    language = language,
                    date = date
                ).first()

                if (response.news.isNotEmpty()) {
                    emit(response)
                    break
                }
                date = DateUtils.formatDateToYesterday()
            }
        }

        val newsList = jsonContainer
            .map { mapper.mapJsonContainerTopNewsToListNews(flow { emit(it) }) }
            .flatMapConcat { newsList -> flow { emit(newsList.map { mapper.mapDtoToDbModel(it) }) } }
            .map { it.distinctBy { it.title } }
            .firstOrNull()

        if (newsList.isNullOrEmpty()) {
            throw Exception("News list is empty or invalid parameters!") // Генерируем ошибку
        }

        newsInfoDao.insertNews(newsList)

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
