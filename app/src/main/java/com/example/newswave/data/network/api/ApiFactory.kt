package com.example.newswave.data.network.api

import com.example.newswave.data.network.model.NewsResponseDto
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {

    private const val BASE_URL = "https://api.worldnewsapi.com/"

    val gson = GsonBuilder()
        .registerTypeAdapterFactory(FlowTypeAdapterFactory())
        .create()

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(BASE_URL)
        .build()

    val apiService= retrofit.create(ApiService :: class.java)

}