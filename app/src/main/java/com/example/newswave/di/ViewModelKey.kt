package com.example.newswave.di

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * Аннотация для ключа ViewModel в Dagger
 * Используется для связывания ViewModel с конкретным классом в Map
 */
@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelKey(val value: KClass<out ViewModel>)
