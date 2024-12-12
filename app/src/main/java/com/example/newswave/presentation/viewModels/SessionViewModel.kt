package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import com.example.newswave.domain.usecases.user.IsUserDataUpdatedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel для управления состоянием сессии приложения
 *
 * Основные задачи:
 * - Отслеживание событий обновления данных пользователя через refreshEvent
 * - Хранение и управление идентификатором активного элемента нижнего меню activeMenuItemId
 */
class SessionViewModel @Inject constructor(
    isUserDataUpdatedUseCase: IsUserDataUpdatedUseCase
): ViewModel() {

    // Поток для обновления данных пользователя
    val refreshEvent: SharedFlow<Unit> = isUserDataUpdatedUseCase()

    // Состояние активного элемента нижнего меню
    private val _activeMenuItemId = MutableStateFlow<Int?>(null)
    val activeMenuItemId: StateFlow<Int?> get() = _activeMenuItemId

    // Метод для установки активного элемента меню
    fun setActiveMenuItemId(itemId: Int) {
        _activeMenuItemId.value = itemId
    }
}