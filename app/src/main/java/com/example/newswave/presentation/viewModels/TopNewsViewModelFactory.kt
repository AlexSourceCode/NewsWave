package com.example.newswave.presentation.viewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//class TopNewsViewModelFactory(
//    private val filterParameter: String,
//    private val valueParameter: String,
//    private val application: Application
//) : ViewModelProvider.Factory {
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(TopNewsViewModel :: class.java)){
//            return TopNewsViewModel(filterParameter,valueParameter,application) as T
//        }
//        throw RuntimeException("Unknown view model class $modelClass")
//    }
//}