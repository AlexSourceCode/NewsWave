package com.example.newswave.presentation.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.source.local.UserPreferences
import com.example.newswave.domain.usecases.user.IsUserDataUpdatedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления состоянием сессии приложения
 *
 * Основные задачи:
 * - Отслеживание событий обновления данных пользователя через refreshEvent
 * - Хранение и управление идентификатором активного элемента нижнего меню activeMenuItemId
 */
class SessionViewModel @Inject constructor(
    isUserDataUpdatedUseCase: IsUserDataUpdatedUseCase,
): ViewModel() {

    // Используем StateFlow, чтобы сохранить последнее состояние
    private val _refreshEvent = MutableStateFlow(false)
    val refreshEvent: StateFlow<Boolean> get() = _refreshEvent

    // Состояние активного элемента нижнего меню
    private val _activeMenuItemId = MutableStateFlow<Int?>(null)
    val activeMenuItemId: StateFlow<Int?> get() = _activeMenuItemId

    init {
        // Подписываемся на UseCase и обновляем refreshEvent
        viewModelScope.launch {
            isUserDataUpdatedUseCase()
                .collect { _refreshEvent.value = true }
        }
    }

    fun resetRefreshEvent() {
        _refreshEvent.value = false
    }

    // Метод для установки активного элемента меню
    fun setActiveMenuItemId(itemId: Int) {
        _activeMenuItemId.value = itemId
    }
}