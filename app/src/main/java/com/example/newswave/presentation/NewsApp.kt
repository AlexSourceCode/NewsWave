package com.example.newswave.presentation

import android.app.Application
import com.example.newswave.di.DaggerApplicationComponent

class NewsApp: Application() {

    val component by lazy {
        DaggerApplicationComponent.factory()
            .create(applicationContext)
    }
}