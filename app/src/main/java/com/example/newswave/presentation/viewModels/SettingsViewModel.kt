package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.dataSource.local.UserPreferences
import com.example.newswave.domain.entity.UserEntity
import com.example.newswave.domain.usecases.FetchUserDataUseCase
import com.example.newswave.domain.usecases.GetContentLanguageUseCase
import com.example.newswave.domain.usecases.GetSourceCountryUseCase
import com.example.newswave.domain.usecases.ObserveAuthStateUseCase
import com.example.newswave.domain.usecases.SaveContentLanguageUseCase
import com.example.newswave.domain.usecases.SaveSourceCountryUseCase
import com.example.newswave.domain.usecases.SignOutUseCase
import com.example.newswave.domain.usecases.SyncUserSettingsUseCase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val fetchUserDataUseCase: FetchUserDataUseCase,
    private val userPreferences: UserPreferences,
    private val getContentLanguageUseCase: GetContentLanguageUseCase,
    private val saveContentLanguageUseCase: SaveContentLanguageUseCase,
    private val syncUserSettingsUseCase: SyncUserSettingsUseCase,
    private val getSourceCountryUseCase: GetSourceCountryUseCase,
    private val saveSourceCountryUseCase: SaveSourceCountryUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private var _user =
        MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user.asStateFlow()

    private var _userData = MutableStateFlow<UserEntity?>(null)
    val userData: StateFlow<UserEntity?> get() = _userData.asStateFlow()

    private val _contentLanguage = MutableStateFlow<String>("")
    val contentLanguage: StateFlow<String> = _contentLanguage

    private val _sourceCountry = MutableStateFlow<String>("")
    val sourceCountry: StateFlow<String> = _sourceCountry

    fun signOut() {
        signOutUseCase()
    }

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

    private fun fetchUserData() {
        viewModelScope.launch {
            fetchUserDataUseCase().collect { userData ->
                _userData.value = userData
            }
        }
    }

    fun getInterfaceLanguage(): String {
        return userPreferences.getInterfaceLanguage()
    }

    fun saveInterfaceLanguage(language: String) {
        userPreferences.saveInterfaceLanguage(language)
    }

    private fun initContentLanguage() {
        viewModelScope.launch {
            getContentLanguageUseCase().collect {
                _contentLanguage.value = it
            }
        }
    }

    fun saveContentLanguage(language: String) {
        saveContentLanguageUseCase(language)
    }

    fun syncUserSettings() {
        syncUserSettingsUseCase()
    }

    private fun initSourceCountry() {
        viewModelScope.launch {
            getSourceCountryUseCase().collect {
                _sourceCountry.value = it
            }
        }
    }

    fun saveSourceCountry(country: String) {
        saveSourceCountryUseCase(country)
    }


    init {
        observeAuthState()
        initSourceCountry()
        initContentLanguage()
    }

}