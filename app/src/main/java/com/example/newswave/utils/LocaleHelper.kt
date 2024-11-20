package com.example.newswave.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.compose.ui.text.toUpperCase
import androidx.work.Configuration
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String): Context { // languageCode тут в маленьком регистре
        Log.d("LocaleHelperLanguageCodeState", languageCode) // сделать условие, если передается system
        val locale = if (languageCode == "system") Locale(getSystemLanguage()) else Locale(languageCode)
        Locale.setDefault(locale)

        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    private fun getSystemLanguage(): String = Resources.getSystem().configuration.locales[0].language



}