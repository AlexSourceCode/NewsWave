package com.example.newswave.data.dataSource.local

import android.content.Context
import android.util.Log
import com.example.newswave.domain.entity.UserEntity
import com.example.newswave.utils.LocaleHelper
import com.google.gson.Gson

/**
 * UserPreferences: Класс для работы с локальными настройками приложения
 */
class UserPreferences(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    companion object {
        private const val PREFERENCES_NAME = "user_prefs"
        private const val KEY_INTERFACE_LANGUAGE = "interface_language"
        private const val KEY_CONTENT_LANGUAGE = "content_language"
        private const val KEY_SOURCE_COUNTRY = "source_country"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val TAG = "UserPreference"
        private const val DEFAULT_LANGUAGE = "ru"

    }

    // Сохранение данных пользователя
    fun saveUserData(user: UserEntity) {
        val json = Gson().toJson(user)
        Log.d(TAG, "saveUser ${json.toString()}")
        editor.putString(KEY_USER_DATA, json)
        editor.apply()
    }

    // Получение данных пользователя
    fun getUserData(): UserEntity? {
        val json = sharedPreferences.getString(KEY_USER_DATA, null)
        Log.d(TAG, "getUser ${json.toString()}")
        return if (json != null) Gson().fromJson(json, UserEntity::class.java) else null
    }

    // Проверка, является ли запуск приложения первым
    private fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    // Меняет флаг после первого запуска
    private fun setFirstLaunchCompleted() {
        editor.putBoolean(KEY_FIRST_LAUNCH, false)
        editor.apply()
    }

    // Инициализация настроек по умолчанию при первом запуске приложения
    fun initializeDefaultSettings() {
        if (isFirstLaunch()) {
            saveInterfaceLanguage(LocaleHelper.SYSTEM_DEFAULT) // Устанавливаем "Как в системе" по умолчанию
            saveContentLanguage(DEFAULT_LANGUAGE)
            saveSourceCountry(DEFAULT_LANGUAGE)
            setFirstLaunchCompleted()
        }
    }


    // Сохранение выбранного языка интерфейса
    fun saveInterfaceLanguage(language: String) {
        editor.putString(KEY_INTERFACE_LANGUAGE, language)
        editor.apply()
    }

    // Получение текущего языка интерфейса
    fun getInterfaceLanguage(): String {
        val savedLanguage = sharedPreferences.getString(KEY_INTERFACE_LANGUAGE, LocaleHelper.SYSTEM_DEFAULT)
        return if (savedLanguage == LocaleHelper.SYSTEM_DEFAULT) {
            LocaleHelper.SYSTEM_DEFAULT
        } else savedLanguage!!
    }

    // Сохранение языка контента
    fun saveContentLanguage(language: String) {
        editor.putString(KEY_CONTENT_LANGUAGE, language)
        editor.apply()
    }

    // Получение текущего языка контента
    fun getContentLanguage(): String {
        return sharedPreferences.getString(KEY_CONTENT_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    // Сохранение страны источника
    fun saveSourceCountry(country: String) {
        editor.putString(KEY_SOURCE_COUNTRY, country)
        editor.apply()
    }

    // Получение текущей страны источника
    fun getSourceCountry(): String {
        return sharedPreferences.getString(KEY_SOURCE_COUNTRY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    // Очистка всех данных пользователя из SharedPreferences
    fun clearUserData() {
        editor.remove(KEY_USER_DATA)
        editor.apply()
        Log.d(TAG, "User data cleared from SharedPreferences")
    }

}