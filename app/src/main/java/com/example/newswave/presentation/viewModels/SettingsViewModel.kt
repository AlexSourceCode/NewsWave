package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.dataSource.local.UserPreferences
import com.example.newswave.domain.entity.UserEntity
import com.example.newswave.domain.usecases.user.FetchUserDataUseCase
import com.example.newswave.domain.usecases.user.GetContentLanguageUseCase
import com.example.newswave.domain.usecases.user.GetSourceCountryUseCase
import com.example.newswave.domain.usecases.user.ObserveAuthStateUseCase
import com.example.newswave.domain.usecases.user.SaveContentLanguageUseCase
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
    private val userPreferences: UserPreferences,
    private val getContentLanguageUseCase: GetContentLanguageUseCase,
    private val saveContentLanguageUseCase: SaveContentLanguageUseCase,
    private val getSourceCountryUseCase: GetSourceCountryUseCase,
    private val saveSourceCountryUseCase: SaveSourceCountryUseCase,
    private val signOutUseCase: SignOutUseCase
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

    init {
        observeAuthState()
        initSourceCountry()
        initContentLanguage()
    }

    // Выполняет выход из учетной записи
    fun signOut() {
        signOutUseCase()
    }

    // Получение языка интерфейса
    fun getInterfaceLanguage(): String {
        return userPreferences.getInterfaceLanguage()
    }

    // Сохранение нового языка интерфейса
    fun saveInterfaceLanguage(language: String) {
        userPreferences.saveInterfaceLanguage(language)
    }

    // Сохранение языка контента
    fun saveContentLanguage(language: String) {
        saveContentLanguageUseCase(language)
    }

    // Сохранение страны источника новостей
    fun saveSourceCountry(country: String) {
        saveSourceCountryUseCase(country)
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
            getContentLanguageUseCase().collect {
                _contentLanguage.value = it
            }
        }
    }

    // Инициализация страны источника новостей из сохраненных настроек
    private fun initSourceCountry() {
        viewModelScope.launch {
            getSourceCountryUseCase().collect {
                _sourceCountry.value = it
            }
        }
    }
}