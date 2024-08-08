package com.example.newswave.presentation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.newswave.data.repository.NewsRepositoryImpl
import com.example.newswave.databinding.ActivityMainBinding
import com.example.newswave.domain.usecases.GetTopNewsList
import com.example.newswave.domain.usecases.LoadDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val repository by lazy { NewsRepositoryImpl(application)}
    private val getTopNews by lazy {  GetTopNewsList(repository)}
    private val loadData by lazy { LoadDataUseCase(repository)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycleScope.launch {
            loadData()
        }

        val testTopNewsList = getTopNews()
        lifecycleScope.launch {
            testTopNewsList
                .flatMapConcat { it.asFlow() }
                .map { it.author }
                .collect { id ->
                    Log.d("MainActivityTest", "$id")
                }
        }

    }
}