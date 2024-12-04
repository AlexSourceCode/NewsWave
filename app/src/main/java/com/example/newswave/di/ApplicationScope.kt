package com.example.newswave.di

import javax.inject.Scope


/**
 * Аннотация Scope для области жизни приложения
 * Используется для ограничений времени жизни зависимостей на уровне всего приложения
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
