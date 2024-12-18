package com.example.newswave.utils

import android.content.Context
import android.util.Patterns
import com.example.newswave.R

// Проверка корректности введённых данных
object InputValidator {
    fun validateRegistrationInput(
        context: Context,
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        showToast: (String) -> Unit
    ): Boolean {
        if (username.length < 3 || username.length > 20) {
            showToast(context.getString(R.string.invalid_username_length))
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(context.getString(R.string.invalid_email_format))
            return false
        }

        if (password.isEmpty()) {
            showToast(context.getString(R.string.invalid_password_length))
            return false
        }

        if (firstName.length < 2 || firstName.length > 20) {
            showToast(context.getString(R.string.invalid_first_name_length))
            return false
        }

        if (lastName.length < 2 || lastName.length > 20) {
            showToast(context.getString(R.string.invalid_last_name_length))
            return false
        }

        return true
    }
}