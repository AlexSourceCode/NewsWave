package com.example.newswave.data.datasource.remote

import com.example.newswave.domain.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Класс для работы с Firebase
 * Обеспечивает методы для авторизации, регистрации, сброса пароля и работы с базой данных
 */
class FirebaseDataSource @Inject constructor(
    private val auth: FirebaseAuth, // Firebase API для управления авторизацией
    private val database: FirebaseDatabase // Firebase API для работы с базой данных Realtime Database.
) {

    // Ссылка на узел Users в Firebase Realtime Database
    private val usersReference = database.getReference("Users")

    // Поток состояния авторизации, содержащий текущего авторизованного пользователя
    private val _authStateFlow = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val authStateFlow: StateFlow<FirebaseUser?> get() = _authStateFlow.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authStateFlow.value = firebaseAuth.currentUser
        }
    }

    // Авторизация по почте
    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(auth.currentUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

     // Регистрация нового пользователя
    suspend fun signUp(email: String, password: String, user: UserEntity): Result<FirebaseUser?> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser
            firebaseUser?.let {
                usersReference.child(it.uid).setValue(user.copy(id = it.uid)).await()
            }
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Сброс пароля
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Выход из системы
    suspend fun signOut() {
        auth.signOut()
    }

    // Получение данных пользователя из базы Firebase
    suspend fun fetchUserData(userId: String): Result<UserEntity?> {
        return try {
            val snapshot = usersReference.child(userId).get().await()
            val user = snapshot.getValue(UserEntity::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Обновление конкретного поля в данных пользователя.
    suspend fun updateUserField(userId: String, field: String, value: String): Result<Unit> {
        return try {
            usersReference.child(userId).child(field).setValue(value).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}















