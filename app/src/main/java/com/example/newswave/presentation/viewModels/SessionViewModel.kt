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

    private val _refreshEvent = MutableSharedFlow<Unit>()
    val refreshEvent: SharedFlow<Unit> = _refreshEvent

    init {
        observeUserDataUpdates()
    }

    private fun observeUserDataUpdates() {
        viewModelScope.launch {
            isUserDataUpdatedUseCase().collect { updated ->
                if (updated) {
                    _refreshEvent.emit(Unit) // Уведомляем о необходимости обновления
                }
            }
        }
    }

    fun notifyRefreshRequired() {
        viewModelScope.launch {
            _refreshEvent.emit(Unit)
        }
    }

}