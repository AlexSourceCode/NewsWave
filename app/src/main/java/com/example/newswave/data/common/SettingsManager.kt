package com.example.newswave.data.common

import android.content.Context

object SettingsManager {

    private const val LANGUAGE_KEY = "language"
    private const val SOURCE_COUNTRY_KEY = "source_country"
    private const val PREFS_NAME = "app_settings"
    private const val UI_LANGUAGE_KEY = "ui_language"

    fun saveSettings(context: Context, language: String, sourceCountry: String, apiKey: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString(LANGUAGE_KEY, language)
            putString(SOURCE_COUNTRY_KEY, sourceCountry)
            apply()
        }
    }

    fun getLanguage(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(LANGUAGE_KEY, "en") ?: "en"
    }

    fun getSourceCountry(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(SOURCE_COUNTRY_KEY, "us") ?: "us"
    }


}