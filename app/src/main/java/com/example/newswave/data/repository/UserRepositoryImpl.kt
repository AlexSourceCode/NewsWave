package com.example.newswave.data.repository

import android.util.Log
import com.example.newswave.data.dataSource.local.UserPreferences
import com.example.newswave.data.dataSource.remote.FirebaseDataSource
import com.example.newswave.data.network.model.ErrorType
import com.example.newswave.domain.entity.UserEntity
import com.example.newswave.domain.repository.LocalDataSource
import com.example.newswave.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Реализация репозитория для управления пользовательскими данными
 * Инкапсулирует логику работы с Firebase, локальной БД и пользовательскими настройками
 */
class UserRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource, // Источник данных из Firebase
    private val userPreferences: UserPreferences, // Локальные пользовательские настройки
    private val localDataSource: LocalDataSource // Локальный источник данных
) : UserRepository {

    private val ioScope = CoroutineScope(Dispatchers.IO) // Контекст для фоновых операций

    // Текущий авторизованный пользователь (Firebase)
    private var _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user.asStateFlow()

    // Потоки ошибок для разных операций
    private val _signInError = MutableSharedFlow<String>()
    private val signInError: SharedFlow<String> get() = _signInError

    private val _forgotPasswordError = MutableSharedFlow<String>()
    private val forgotPasswordError: SharedFlow<String> get() = _forgotPasswordError

    private val _signUpError = MutableSharedFlow<String>()
    private val signUpError: SharedFlow<String> get() = _signUpError

    // Данные пользователя
    private var _userData = MutableStateFlow<UserEntity?>(null)
    private val userData: StateFlow<UserEntity?> get() = _userData.asStateFlow()

    // Поток для уведомлений об успешности операций
    private var _isSuccess = MutableSharedFlow<Boolean>()
    private val isSuccess: SharedFlow<Boolean> get() = _isSuccess.asSharedFlow()

    // Локальные настройки языка и страны
    private val _contentLanguage = MutableStateFlow<String>(getContentLanguage())
    private val contentLanguage: StateFlow<String> = _contentLanguage

    private val _sourceCountry = MutableStateFlow<String>(getSourceCountry())
    private val sourceCountry: StateFlow<String> = _sourceCountry

    // Поток для уведомления об обновлении данных пользователя
    private val _isUserDataUpdatedFlow = MutableSharedFlow<Unit>()
    private val isUserDataUpdatedFlow: SharedFlow<Unit> = _isUserDataUpdatedFlow

    companion object {
        private const val DEFAULT_LANGUAGE = "ru"
    }

    // Сброс пароля для пользователя. Отправляет запрос в Firebase
    override fun resetPassword(email: String) {
        ioScope.launch {
            val result = firebaseDataSource.resetPassword(email)
            result.onSuccess {
                _isSuccess.emit(true)
            }.onFailure {
                _forgotPasswordError.emit(it.message.toString())
            }
        }

    }

    // Вход пользователя по email и паролю
    // Успешный вход удаляет локальные данные и уведомляет о смене состояния
    override fun signInByEmail(email: String, password: String) {
        ioScope.launch {
            val result = firebaseDataSource.signIn(email, password)
            result.onSuccess {
                _isUserDataUpdatedFlow.emit(Unit)
                localDataSource.deleteAllNews()
            }.onFailure {
                _signInError.emit(it.message.orEmpty())
            }
        }
    }

    // Регистрация нового пользователя
    // Создаёт пользователя в Firebase и сохраняет его данные локально
    override fun signUpByEmail(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ) {
        val user = UserEntity(
            id = "",
            username = username,
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            newsContent = DEFAULT_LANGUAGE,
            newsSourceCountry = DEFAULT_LANGUAGE
        )
        ioScope.launch {
            val result = firebaseDataSource.signUp(email, password, user)
            result.onSuccess { firebaseUser ->
                _isUserDataUpdatedFlow.emit(Unit) // Уведомляем, что данные обновлены
                localDataSource.deleteAllNews()
                if (firebaseUser != null) {
                    userPreferences.saveUserData(user.copy(id = firebaseUser.uid))
                }
            }
            result.onFailure {
                _signUpError.emit(it.message.toString())
            }
        }
    }

    // Выход пользователя
    // Очищает данные и сбрасывает состояние
    override fun signOut() {
        ioScope.launch {
            firebaseDataSource.signOut()
            localDataSource.deleteAllNews()
            userPreferences.clearUserData()
            _userData.emit(null)
            _user.value = null
        }

    }

    // Получение текущего языка контента
    override fun getContentLanguage(): String {
        val content =
            userPreferences.getUserData()?.newsContent ?: userPreferences.getContentLanguage()
        return content
    }

    // Этот поток используется для отслеживания обновлений данных в приложении
    override fun isUserDataUpdated(): SharedFlow<Unit> {
        return isUserDataUpdatedFlow
    }

    // Обновление локального языка контента и его синхронизация с Firebase
    override fun saveContentLanguage(language: String) {
        _contentLanguage.value = language
        userPreferences.saveContentLanguage(language)
        ioScope.launch {
            firebaseDataSource.authStateFlow.value?.uid?.let { userId ->
                updateSingleField(userId, "newsContent", language)
                userPreferences.getUserData()?.copy(newsContent = language)?.let {
                    updateUserPreferences(it)
                }
            }
        }
    }

    // Получает текущую страну источника новостей из локальных пользовательских настроек
    override fun getSourceCountry(): String {
        return userPreferences.getUserData()?.newsSourceCountry
            ?: userPreferences.getSourceCountry()
    }

    // Сохраняет выбранную страну источника новостей
    // Обновляет данные в локальных настройках и синхронизирует их с Firebase
    //Также очищает локальный кэш новостей
    override fun saveSourceCountry(country: String) {
        _sourceCountry.value = country
        userPreferences.saveSourceCountry(country)

        ioScope.launch {
            localDataSource.deleteAllNews()
            firebaseDataSource.authStateFlow.value?.uid?.let { userId ->
                updateSingleField(userId, "newsSourceCountry", country)
            }
            userPreferences.getUserData()?.copy(newsSourceCountry = country)?.let {
                updateUserPreferences(it)
            }
        }
    }

    // Асинхронно обновляет одно поле пользовательских данных в Firebase.
    private suspend fun updateSingleField(userId: String, field: String, value: String) {
        localDataSource.deleteAllNews()
        firebaseDataSource.updateUserField(userId, field, value)
    }

    // Предоставляет поток уведомлений об успешности операций авторизации
    // Используется для информирования интерфейса пользователя о результате операций входа/регистрации
    override fun fetchIsSuccessAuth(): SharedFlow<Boolean> {
        return isSuccess
    }


    // Наблюдает за текущим состоянием авторизации в Firebase
    // Предоставляет поток с информацией о текущем авторизованном пользователе
    override fun observeAuthState(): StateFlow<FirebaseUser?> {
        return firebaseDataSource.authStateFlow
    }

    // Предоставляет поток ошибок для указанного типа операции
    override fun fetchError(type: ErrorType): SharedFlow<String> {
        return when (type) {
            ErrorType.SIGN_IN -> signInError
            ErrorType.SIGN_UP -> signUpError
            ErrorType.FORGOT_PASSWORD -> forgotPasswordError
        }
    }

    // Получает поток текущих данных пользователя
    // Данные извлекаются из локальных пользовательских настроек
    override fun fetchUserData(): StateFlow<UserEntity?> {
        val user = userPreferences.getUserData()
        _userData.value = user
        return userData
    }

    // Получает поток текущего языка контента
    override fun fetchContentLanguage(): StateFlow<String> {
        return contentLanguage
    }

    // Получает поток текущей страны источника новостей
    override fun fetchSourceCountry(): StateFlow<String> {
        return sourceCountry
    }

    // Сохранение данных пользователя в локальных настройках
    private suspend fun updateUserPreferences(user: UserEntity) {
        userPreferences.saveSourceCountry(user.newsSourceCountry)
        userPreferences.saveContentLanguage(user.newsContent)
        userPreferences.saveUserData(user)

        _contentLanguage.emit(user.newsContent)
        _sourceCountry.emit(user.newsSourceCountry)
        _userData.value = user
    }

    // Загрузка данных пользователя из Firebase и их обновление локально
    private suspend fun updateUserData(userId: String) {
        val result = firebaseDataSource.fetchUserData(userId)
        result.onSuccess { userEntity ->
            userEntity?.let {
                updateUserPreferences(it)
                _isUserDataUpdatedFlow.emit(Unit)
            }
        }.onFailure { error ->
            Log.e("UserRepositoryImpl", "Failed to fetch user data: ${error.message}")
        }
    }


    init {
        fetchUserData()
        // Подписка на изменения состояния авторизации в Firebase
        ioScope.launch {
            firebaseDataSource.authStateFlow
                .collect { firebaseUser ->
                    firebaseUser?.let { user ->
                        if (_userData.value?.id != user.uid) {
                            updateUserData(user.uid)
                        }
                    }
                }
        }
    }
}