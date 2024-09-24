package com.example.newswave.data.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.database.dbNews.NewsDb
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.mapper.flattenToList
import com.example.newswave.data.network.api.ApiFactory
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.network.model.TopNewsResponseDto
import com.example.newswave.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RefreshDataWorker(
    context: Context,
    private var workerParameters: WorkerParameters,
    private val apiService: ApiService,
    private val newsInfoDao: NewsDao,
    private val mapper: NewsMapper
) : CoroutineWorker(context, workerParameters) {


    override suspend fun doWork(): Result {
        return try {
            loadData()
            Result.success()
        } catch (e: Exception){
            Log.e("CheckNews", "Error fetching top news", e)
            Result.failure()
        }
    }


    private suspend fun loadData(){
        val jsonContainer: Flow<TopNewsResponseDto> =
            apiService.getListTopNews(date = "2024-09-23") // Временно другая дата, так как сегодня еще нет новостей
        val newsListDbModel =
            jsonContainer //преобразование из Flow<NewsResponseDto> в Flow<List<NewsItemDto>>
                .map {
                    mapper.mapJsonContainerTopNewsToListNews(jsonContainer)
                }//преобразование в List<NewsItemDto>
                .flatMapConcat { newList ->
                    flow {
                        emit(newList.map { mapper.mapDtoToDbModel(it) }) //преобразование в List<NewsDbModel>
                    }
                }
                .flattenToList()// преобразование из flow в list
                .distinctBy { it.title }
        //преобразование в List<NewsDbModel>
        newsInfoDao.insertNews(newsListDbModel)
        delay(10000)
    }

    companion object {
        const val WORK_NAME = "refresh news"

        fun makeRequest(): OneTimeWorkRequest { // в качестве параметра можно будет передавать вчерашнюю дату, если сегодняшние новости закончились
            return OneTimeWorkRequestBuilder<RefreshDataWorker>().apply {
                setConstraints(makeConstraints())
            }.build()
        }

        private fun makeConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // как сделать так, чтобы запросы шли только если есть интернет
                .build()
        }
    }
}