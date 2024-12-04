package com.example.newswave.data.network.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Фабрика для создания объекта Retrofit и API-сервиса
 */
object ApiFactory {

    private const val BASE_URL = "https://api.worldnewsapi.com/"

    // Gson с кастомным TypeAdapter для работы с Flow
    val gson = GsonBuilder()
        .registerTypeAdapterFactory(FlowTypeAdapterFactory())
        .create()

    // Экземпляр Retrofit для выполнения HTTP-запросов
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(BASE_URL)
        .build()

    // API-сервис для работы с новостями.
    val apiService= retrofit.create(ApiService :: class.java)

}