package com.example.newswave.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.compose.ui.text.toUpperCase
import androidx.work.Configuration
import java.util.Locale

object LocaleHelper {

    const val SYSTEM_DEFAULT = "system"

    fun setLocale(context: Context, languageCode: String): Context { // languageCode тут в маленьком регистре
        val locale = if (languageCode == SYSTEM_DEFAULT) Locale(getSystemLanguage()) else Locale(languageCode)
        Locale.setDefault(locale)

        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
    fun getSystemLanguage(): String = Resources.getSystem().configuration.locales[0].language
}