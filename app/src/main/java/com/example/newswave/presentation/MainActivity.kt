package com.example.newswave.presentation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.newswave.R
import com.example.newswave.data.repository.NewsRepositoryImpl
import com.example.newswave.databinding.ActivityMainBinding
import com.example.newswave.domain.usecases.GetTopNewsList
import com.example.newswave.domain.usecases.LoadDataUseCase
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AndroidThreeTen.init(this)

        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)




//        lifecycleScope.launch {
//            loadData()
//        }
//
//        val testTopNewsList = getTopNews()
//        lifecycleScope.launch {
//            testTopNewsList
//                .flatMapConcat { it.asFlow() }
//                .map { it.author }
//                .collect { id ->
//                    Log.d("MainActivityTest", "$id")
//                }
//        }

    }

}