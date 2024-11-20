package com.example.newswave.data.database.dbNews

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.compose.ui.text.toUpperCase
import com.example.newswave.domain.entity.UserEntity
import com.example.newswave.utils.LocaleHelper
import com.google.gson.Gson
import java.util.Locale
import javax.inject.Inject

class UserPreferences(private val context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    companion object {
        private const val PREFERENCES_NAME = "user_prefs" // поменять название на user_preference
        private const val KEY_INTERFACE_LANGUAGE = "interface_language"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val SYSTEM_DEFAULT = "system"
    }

    fun saveUserData(user: UserEntity) {
        val json = Gson().toJson(user)
        editor.putString(KEY_USER_DATA, json)
        editor.apply()
    }

    fun getUserData(): UserEntity? {
        val json = sharedPreferences.getString(KEY_USER_DATA, null)
        return if (json != null) Gson().fromJson(json, UserEntity::class.java) else null
    }

    private fun isFirstLaunch(): Boolean { // проверка флага, является ли запуск приложения первым
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    private fun setFirstLaunchCompleted() { // меняет флаг после первого запуска
        editor.putBoolean(KEY_FIRST_LAUNCH, false)
        editor.apply()
    }

    fun initializeDefaultSettings() { // Если это первый запуск, то устанавливается язык устройства системы
        if (isFirstLaunch()) {
            Log.d("UserPreferences", "executeflag")
            saveInterfaceLanguage(SYSTEM_DEFAULT) // Устанавливаем "Как в системе" по умолчанию
//            saveContentLanguage("ru") // По умолчанию язык контента — "ru"
//            saveSourceCountry("ru") // По умолчанию страна источника — "ru"
            setFirstLaunchCompleted() // Фиксируем, что первый запуск завершен
        }
    }

    fun saveInterfaceLanguage(language: String) { // Изменяет текущий язык
        editor.putString(KEY_INTERFACE_LANGUAGE, language)
        editor.apply()
    }

    fun getInterfaceLanguage(): String { //Получение текущего языка приложение, если оно не установленно, то по умолчанию берется язык устройства системы
        val savedLanguage = sharedPreferences.getString(KEY_INTERFACE_LANGUAGE, SYSTEM_DEFAULT)
        return if (savedLanguage == SYSTEM_DEFAULT) {
            SYSTEM_DEFAULT
        } else savedLanguage!!
    }

}