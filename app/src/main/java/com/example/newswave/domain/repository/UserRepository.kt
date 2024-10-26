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

    fun fetchIsSuccessAuth(): SharedFlow<Boolean>

    fun observeAuthState(): StateFlow<FirebaseUser?>

    fun fetchErrorAuth(): SharedFlow<String>

    fun fetchUserData(): StateFlow<UserEntity?>

}