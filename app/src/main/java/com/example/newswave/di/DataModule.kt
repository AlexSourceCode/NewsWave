package com.example.newswave.di

import android.app.Application
import android.content.Context
import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.database.dbNews.NewsDb
import com.example.newswave.data.network.api.ApiFactory
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.repository.NewsRepositoryImpl
import com.example.newswave.domain.repository.NewsRepository
import com.example.newswave.domain.usecases.SearchNewsByFilterUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @Binds
    @ApplicationScope
    fun bindNewsRepository(newsRepositoryImpl: NewsRepositoryImpl): NewsRepository

    companion object{
        @Provides
        @ApplicationScope
        fun provideNewsDao(application: Application): NewsDao{
            return NewsDb.getInstance(application).newsDao()
        }

        @Provides
        fun provideApplication(context: Context): Application {
            return context.applicationContext as Application
        }

        @Provides
        @ApplicationScope
        fun provideApiService(): ApiService{
            return ApiFactory.apiService
        }
    }
}