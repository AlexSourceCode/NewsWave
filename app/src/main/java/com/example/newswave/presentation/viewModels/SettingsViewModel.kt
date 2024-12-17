package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.entities.UserEntity
import com.example.newswave.domain.usecases.user.ClearUserRepositoryUseCase
import com.example.newswave.domain.usecases.user.FetchInterfaceLanguageUseCase
import com.example.newswave.domain.usecases.user.FetchUserDataUseCase
import com.example.newswave.domain.usecases.user.FetchContentLanguageUseCase
import com.example.newswave.domain.usecases.user.FetchSourceCountryUseCase
import com.example.newswave.domain.usecases.user.ObserveAuthStateUseCase
import com.example.newswave.domain.usecases.user.SaveContentLanguageUseCase
import com.example.newswave.domain.usecases.user.SaveInterfaceLanguageUseCase
import com.example.newswave.domain.usecases.user.SaveSourceCountryUseCase
import com.example.newswave.domain.usecases.user.SignOutUseCase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления настройками пользователя в приложении
 * Обеспечивает взаимодействие с данными пользователя, языковыми настройками и состоянием авторизации
 */
class SettingsViewModel @Inject constructor(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val fetchUserDataUseCase: FetchUserDataUseCase,
    private val fetchContentLanguageUseCase: FetchContentLanguageUseCase,
    private val saveContentLanguageUseCase: SaveContentLanguageUseCase,
    private val fetchSourceCountryUseCase: FetchSourceCountryUseCase,
    private val saveSourceCountryUseCase: SaveSourceCountryUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val clearUserRepositoryUseCase: ClearUserRepositoryUseCase,
    private val saveInterfaceLanguageUseCase: SaveInterfaceLanguageUseCase,
    private val fetchInterfaceLanguageUseCase: FetchInterfaceLanguageUseCase
) : ViewModel() {

    // Хранит текущее состояние авторизованного пользователя
    private var _user =
        MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user.asStateFlow()

    // Хранит данные пользователя
    private var _userData = MutableStateFlow<UserEntity?>(null)
    val userData: StateFlow<UserEntity?> get() = _userData.asStateFlow()

    // Хранит текущий язык контента
    private val _contentLanguage = MutableStateFlow<String>("")
    val contentLanguage: StateFlow<String> = _contentLanguage

    // Хранит текущую страну источника новостей
    private val _sourceCountry = MutableStateFlow<String>("")
    val sourceCountry: StateFlow<String> = _sourceCountry

    private val _interfaceLanguage = MutableStateFlow<String>("")
    val interfaceLanguage: StateFlow<String> get() = _interfaceLanguage.asStateFlow()

    init {
        observeAuthState()
        initSourceCountry()
        initContentLanguage()
        initInterfaceLanguage()
    }

    // Выполняет выход из учетной записи
    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }

    // Сохранение нового языка интерфейса
    fun saveInterfaceLanguage(language: String) {
        viewModelScope.launch {
            saveInterfaceLanguageUseCase(language)
        }
    }

    // Сохранение языка контента
    fun saveContentLanguage(language: String) {
        viewModelScope.launch {
            saveContentLanguageUseCase(language)
        }
    }

    // Сохранение страны источника новостей
    fun saveSourceCountry(country: String) {
        viewModelScope.launch {
            saveSourceCountryUseCase(country)
        }
    }

    // Наблюдение за состоянием авторизации пользователя
    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { stateAuth ->
                _user.value = stateAuth
                if (stateAuth != null) {
                    fetchUserData()
                }
            }
        }
    }

    // Загрузка данных пользователя из источника
    private fun fetchUserData() {
        viewModelScope.launch {
            fetchUserDataUseCase().collect { userData ->
                _userData.value = userData
            }
        }
    }

    // Инициализация языка контента из сохраненных настроек
    private fun initContentLanguage() {
        viewModelScope.launch {
            fetchContentLanguageUseCase().collect {
                _contentLanguage.value = it
            }
        }
    }

    // Инициализация страны источника новостей из сохраненных настроек
    private fun initSourceCountry() {
        viewModelScope.launch {
            fetchSourceCountryUseCase().collect {
                _sourceCountry.value = it
            }
        }
    }

    private fun initInterfaceLanguage() {
        viewModelScope.launch {
            fetchInterfaceLanguageUseCase().collect {
                _interfaceLanguage.value = it
            }
        }
    }

    override fun onCleared() {
        clearUserRepositoryUseCase()
        super.onCleared()
    }
}