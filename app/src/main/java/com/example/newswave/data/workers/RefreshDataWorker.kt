package com.example.newswave.data.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.newswave.data.network.api.ApiService

//class RefreshDataWorker(
//    context: Context,
//    workerParameters: WorkerParameters,
//    private val api: ApiService,
//
//    ): Worker(context, workerParameters) {
//    override fun doWork(): Result {
//
//    }
//}