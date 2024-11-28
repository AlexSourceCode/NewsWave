package com.example.newswave.domain.repository

import com.example.newswave.domain.entity.UserEntity
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    fun resetPassword(email: String)

    fun signInByEmail(email: String, password: String)

    fun signUpByEmail(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    )

    fun signOut()

    fun syncUserSettings()

    fun getContentLanguage(): String

    fun isUserDataUpdated(): SharedFlow<Unit>

    fun saveContentLanguage(language: String)

    fun getSourceCountry(): String

    fun saveSourceCountry(country: String)

    fun fetchIsSuccessAuth(): SharedFlow<Boolean>

    fun observeAuthState(): StateFlow<FirebaseUser?>

    fun fetchErrorSignIn(): SharedFlow<String>

    fun fetchErrorSignUp(): SharedFlow<String>

    fun fetchErrorForgotPassword(): SharedFlow<String>

    fun fetchUserData(): StateFlow<UserEntity?>

    fun fetchContentLanguage(): StateFlow<String>

    fun fetchSourceCountry(): StateFlow<String>

}