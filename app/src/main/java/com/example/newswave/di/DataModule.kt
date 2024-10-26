package com.example.newswave.di

import android.app.Application
import android.content.Context
import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.database.dbNews.NewsDb
import com.example.newswave.data.network.api.ApiFactory
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.repository.NewsRepositoryImpl
import com.example.newswave.data.repository.SubscriptionRepositoryImpl
import com.example.newswave.data.repository.UserRepositoryImpl
import com.example.newswave.domain.repository.NewsRepository
import com.example.newswave.domain.repository.SubscriptionRepository
import com.example.newswave.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides

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

        @Provides
        fun provideFirebaseAuth(): FirebaseAuth{
            return Firebase.auth
        }

        @Provides
        fun provideFirebaseDatabase(): FirebaseDatabase {
            return Firebase.database
        }
    }
}
