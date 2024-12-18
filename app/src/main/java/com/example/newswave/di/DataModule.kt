package com.example.newswave.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.newswave.data.source.local.NewsDao
import com.example.newswave.data.source.local.NewsDb
import com.example.newswave.data.source.local.UserPreferences
import com.example.newswave.data.network.api.ApiFactory
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.repositories.LocalDataSourceImpl
import com.example.newswave.data.repositories.NewsRepositoryImpl
import com.example.newswave.data.repositories.RemoteDataSourceImpl
import com.example.newswave.data.repositories.SubscriptionRepositoryImpl
import com.example.newswave.data.repositories.UserRepositoryImpl
import com.example.newswave.domain.repositories.LocalDataSource
import com.example.newswave.domain.repositories.NewsRepository
import com.example.newswave.domain.repositories.RemoteDataSource
import com.example.newswave.domain.repositories.SubscriptionRepository
import com.example.newswave.domain.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides

/**
 * Dagger модуль для предоставления зависимостей, связанных с данными.
 */
@Module
interface DataModule {

    @Binds
    @ApplicationScope
    fun bindNewsRepository(newsRepositoryImpl: NewsRepositoryImpl): NewsRepository

    @Binds
    @ApplicationScope
    fun bindSubscriptionRepository(subscriptionRepository: SubscriptionRepositoryImpl): SubscriptionRepository

    @Binds
    @ApplicationScope
    fun bindUserRepository(userRepository: UserRepositoryImpl): UserRepository

    @Binds
    @ApplicationScope
    fun bindLocalDataSource(localDataSource: LocalDataSourceImpl): LocalDataSource

    @Binds
    @ApplicationScope
    fun bindRemoteDataSource(remoteDataSource: RemoteDataSourceImpl): RemoteDataSource

    companion object {
        @Provides
        @ApplicationScope
        fun provideNewsDao(application: Application): NewsDao {
            return NewsDb.getInstance(application).newsDao()
        }

        @Provides
        fun provideApplication(context: Context): Application {
            return context.applicationContext as Application
        }

        @Provides
        @ApplicationScope
        fun provideApiService(): ApiService {
            return ApiFactory.apiService
        }

        @Provides
        fun provideFirebaseAuth(): FirebaseAuth {
            return Firebase.auth
        }

        @Provides
        fun provideFirebaseDatabase(): FirebaseDatabase {
            return Firebase.database
        }

        @Provides
        fun provideSavedStateHandle(): SavedStateHandle {
            return SavedStateHandle()
        }

        @Provides
        fun provideUserPreferences(application: Application): UserPreferences {
            return UserPreferences(application)
        }

    }
}
