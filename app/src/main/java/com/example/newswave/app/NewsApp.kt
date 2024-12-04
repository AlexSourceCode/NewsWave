package com.example.newswave.app

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import com.example.newswave.data.dataSource.local.UserPreferences
import com.example.newswave.data.workers.RefreshDataWorkerFactory
import com.example.newswave.di.DaggerApplicationComponent
import com.example.newswave.domain.repository.UserRepository
import javax.inject.Inject

class NewsApp: Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: RefreshDataWorkerFactory
    @Inject
    lateinit var userRepository: UserRepository

    val component by lazy {
        DaggerApplicationComponent.factory()
            .create(
                applicationContext,
                getSharedPreferences("news_by_search", Context.MODE_PRIVATE)
                )
    }

    override fun onCreate() {
        component.inject(this)
        super.onCreate()

        val userPreferences = UserPreferences(this)
        userPreferences.initializeDefaultSettings()
//        syncUserDataIfNeeded()


    }

//    private fun syncUserDataIfNeeded() { //под вопросом
//        val currentUser = userRepository.observeAuthState().value
//
//        if (currentUser != null) {
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    userRepository.syncUserSettings()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


}