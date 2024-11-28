package com.example.newswave.presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.usecases.IsUserDataUpdatedUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionViewModel @Inject constructor(
    private val isUserDataUpdatedUseCase: IsUserDataUpdatedUseCase
): ViewModel() {

    val refreshEvent: SharedFlow<Unit> = isUserDataUpdatedUseCase()
//
//    init {
//        observeUserDataUpdates()
//    }
//
//    private fun observeUserDataUpdates() {
//        viewModelScope.launch {
//            isUserDataUpdatedUseCase().collect { updated ->
//                    _refreshEvent.emit(Unit) // Уведомляем о необходимости обновления
//            }
//        }
//    }

}