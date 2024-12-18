package com.example.newswave.data.mapper

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.toList

/**
 * Расширение для работы с Flow<List<T>>
 * Предоставляет удобный метод для преобразования потока списков в единый список элементов
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> Flow<List<T>>.flattenToList(): List<T> = this
    .flatMapConcat { it.asFlow() } // Преобразует каждый список в поток элементов
    .toList() // Собирает все элементы в единый список
