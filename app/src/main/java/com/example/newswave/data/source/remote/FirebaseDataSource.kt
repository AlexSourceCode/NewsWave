package com.example.newswave.data.source.remote

import android.util.Log
import com.example.newswave.di.ApplicationScope
import com.example.newswave.domain.entities.AuthorItemEntity
import com.example.newswave.domain.entities.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Класс для работы с Firebase
 * Обеспечивает методы для авторизации, регистрации, сброса пароля и работы с базой данных
 */
@ApplicationScope
class FirebaseDataSource @Inject constructor(
    private val auth: FirebaseAuth, // Firebase API для управления авторизацией
    private val database: FirebaseDatabase, // Firebase API для работы с базой данных Realtime Database
) {
    private var authorsJob: Job? = null
    private val ioScope = CoroutineScope(Dispatchers.IO) // Определяет контекст для корутин на IO-потоке

    // Ссылка на узел Users в Firebase Realtime Database
    private val usersReference = database.getReference("Users")

    // Получает поток авторов для текущего пользователя
    private fun getAuthorsReference(userId: String) = database.getReference("Authors").child(userId)

    // Поток состояния авторизации, содержащий текущего авторизованного пользователя
    private val _authStateFlow = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val authStateFlow: StateFlow<FirebaseUser?> get() = _authStateFlow.asStateFlow()

    // Поток, содержащий список авторов для текущего пользователя
    private val _authorsFlow = MutableSharedFlow<List<AuthorItemEntity>?>()
    private val authorsFlow: SharedFlow<List<AuthorItemEntity>?> get() = _authorsFlow.asSharedFlow()

    init {
        observeAuthState()
    }

    // Возвращает поток списка авторов текущего пользователя
    suspend fun getAuthorListFlow(): SharedFlow<List<AuthorItemEntity>?> {
        showAuthorsList()
        return authorsFlow
    }

    // Эмитирует список авторов текущего пользователя.
    // Отменяет предыдущую задачу, если она запущена, чтобы избежать накопления задач.
    private suspend fun showAuthorsList(){
        authorsJob?.cancel()
        authorsJob = ioScope.launch {
            val currentUser = authStateFlow.value
            val userId = currentUser?.uid
            val authors = if (userId != null) fetchAuthors(userId) else null
            _authorsFlow.emit(authors)
        }
    }

    // Наблюдает за изменениями состояния аутентификации пользователя.
    private fun observeAuthState() {
        auth.addAuthStateListener { auth ->
            _authStateFlow.value = auth.currentUser
            if (auth.currentUser != null) {
                observeAuthors(auth.currentUser!!.uid)
            } else {
                CoroutineScope(Dispatchers.IO).launch { _authorsFlow.emit(null) }
            }
        }
    }

    // Наблюдает за изменениями списка авторов в Firebase.
    private fun observeAuthors(userId: String) {
        val authorsReference = getAuthorsReference(userId)
        authorsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val authors = snapshot.children.mapNotNull { it.getValue(AuthorItemEntity::class.java) }
                CoroutineScope(Dispatchers.IO).launch {
                    _authorsFlow.emit(authors)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseDataSource", "Failed to listen for authors: ", error.toException())
            }
        })
    }

    // Добавляет автора в список любимых авторов для текущего пользователя в Firebase
    suspend fun addAuthor(userId: String, author: AuthorItemEntity){
        try {
            getAuthorsReference(userId).push().setValue(author).await()
        } catch (e: Exception){
            Log.d("FirebaseDataSource", "Failed to add author: ${e.message}", )
        }
    }

    // Удаляет автора из списка любимых авторов для текущего пользователя из Firebase
    suspend fun deleteAuthor(userId: String, author: String) {
        val query = getAuthorsReference(userId).orderByChild("author").equalTo(author)
        suspendCoroutine<Unit> { continuation ->
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { it.ref.removeValue() }
                    continuation.resume(Unit)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("FirebaseDataSource", "Failed to delete on the author: ${error.message.toString()}", )
                }
            })
        }
    }

    // Получает список авторов для текущего пользователя из Firebase
    suspend fun fetchAuthors(userId: String): List<AuthorItemEntity> {
        val snapshot = getAuthorsReference(userId).get().await()
        return snapshot.children.mapNotNull { it.getValue(AuthorItemEntity::class.java) }
    }

    // Проверяет, является ли указанный автор избранным для текущего пользователя.
    suspend fun isFavoriteAuthor(userId: String, author: String): Boolean {
        val authors = fetchAuthors(userId)
        return authors.any { it.author == author }
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
                usersReference.child(firebaseUser.uid).setValue(user.copy(id = firebaseUser.uid))
                    .await()
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
    fun signOut() {
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