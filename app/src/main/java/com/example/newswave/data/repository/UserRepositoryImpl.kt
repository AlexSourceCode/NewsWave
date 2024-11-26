package com.example.newswave.data.repository

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.newswave.data.database.dbNews.NewsDao
import com.example.newswave.data.database.dbNews.UserPreferences
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.entity.UserEntity
import com.example.newswave.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val dataBase: FirebaseDatabase,
    private val userPreferences: UserPreferences,
    private val newsDao: NewsDao
) : UserRepository {

    private val usersReference = dataBase.getReference("Users")

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user.asStateFlow()

    private val _signInError = MutableSharedFlow<String>()
    val signInError: SharedFlow<String> get() = _signInError

    private val _forgotPasswordError = MutableSharedFlow<String>()
    val forgotPasswordError: SharedFlow<String> get() = _forgotPasswordError

    private val _signUpError = MutableSharedFlow<String>()
    val signUpError: SharedFlow<String> get() = _signUpError

    private var _userData = MutableStateFlow<UserEntity?>(null)
    val userData: StateFlow<UserEntity?> get() = _userData.asStateFlow()

    private var _isSuccess = MutableSharedFlow<Boolean>()
    val isSuccess: SharedFlow<Boolean> get() = _isSuccess.asSharedFlow()

    private val _contentLanguage = MutableStateFlow<String>(getContentLanguage())
    val contentLanguage: StateFlow<String> = _contentLanguage

    private val _sourceCountry = MutableStateFlow<String>(getSourceCountry())
    val sourceCountry: StateFlow<String> = _sourceCountry

    private val _isUserDataUpdatedFlow = MutableStateFlow(false)
    val isUserDataUpdatedFlow: StateFlow<Boolean> = _isUserDataUpdatedFlow

    companion object {
        private const val DEFAULT_LANGUAGE = "ru"
    }

    override fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                ioScope.launch {
                    Log.d("CheckErrorState", "success execute from UserRepositoryImpl")
                    _isSuccess.emit(true)
                }
            }
            .addOnFailureListener { error ->
                ioScope.launch {
                    Log.d("CheckErrorState", "failed execute from UserRepositoryImpl")
                    _forgotPasswordError.emit(error.message.toString())
                }
            }
    }


    override fun signInByEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {}
            .addOnFailureListener { error ->
                ioScope.launch {
                    Log.d("CheckErrorState", " execute from signInByEmailImpl")
                    _signInError.emit(error.message.toString())
                }
            }
    }

    override fun signUpByEmail(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebase = authResult.user
                if (firebase != null) {
                    val user = UserEntity(
                        id = firebase.uid,
                        username,
                        email,
                        password,
                        firstName,
                        lastName,
                        DEFAULT_LANGUAGE,
                        DEFAULT_LANGUAGE
                    )
                    usersReference.child(user.id).setValue(user)
                    userPreferences.saveUserData(
                        UserEntity(
                            id = firebase.uid,
                            username = username,
                            email = email,
                            firstName = firstName,
                            lastName = lastName,
                            newsContent = DEFAULT_LANGUAGE,
                            newsSourceCountry = DEFAULT_LANGUAGE
                        )
                    )
                }
            }
            .addOnFailureListener { error ->
                ioScope.launch {
                    Log.d("CheckErrorState", " execute from signUpByEmail Impl")
                    _signUpError.emit(error.message.toString())
                }
            }
    }

    override fun signOut() {
        userPreferences.clearUserData()

        auth.signOut()

        ioScope.launch {
            _user.emit(null)
            _userData.emit(null)
        }

    }

    override fun syncUserSettings() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            usersReference.child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userEntity = snapshot.getValue<UserEntity>()
                        if (userEntity != null) {
                            userPreferences.saveUserData(userEntity)
                            ioScope.launch {
                                _userData.emit(userEntity)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
//                        ioScope.launch {
//                            _error.emit("Failed to sync user settings: ${error.message}")
//                        }
                    }
                })
        } else {
//            ioScope.launch {
//                _error.emit("No authenticated user found to sync settings.")
//            }
        }
    }

    override fun getContentLanguage(): String {
        val content =userPreferences.getUserData()?.newsContent ?: userPreferences.getContentLanguage()
        Log.d("CheckChangedUserData", "getContentLanguageFromImpl $content")
        return content
    }

    override fun isUserDataUpdated(): StateFlow<Boolean> {
        return isUserDataUpdatedFlow
    }

    override fun saveContentLanguage(language: String) {
        _contentLanguage.value = language
        userPreferences.saveContentLanguage(language)
        ioScope.launch {
            try {
                newsDao.deleteAllNews()
            } catch (e: Exception) {
                Log.e("DatabaseOperation", "Ошибка при очистке базы данных: ${e.message}", e)
            }
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            usersReference.child(currentUser.uid).child("newsContent").setValue(language)
                .addOnSuccessListener {
                    val updatedUser = userPreferences.getUserData()?.copy(newsContent = language)
                    if (updatedUser != null) {
                        userPreferences.saveUserData(updatedUser)
                        ioScope.launch {
                            _userData.value = updatedUser
                        }
                    }
                }
                .addOnFailureListener { error ->
                    ioScope.launch {
//                        _error.emit("Failed to save content language: ${error.message}")
                    }
                }
        } else {
        }
    }

    override fun getSourceCountry(): String {
        return userPreferences.getUserData()?.newsSourceCountry
            ?: userPreferences.getSourceCountry()
    }

    override fun saveSourceCountry(country: String) {
        _sourceCountry.value = country
        userPreferences.saveSourceCountry(country)
        ioScope.launch {
            try {
                newsDao.deleteAllNews()
            } catch (e: Exception) {
                Log.e("DatabaseOperation", "Ошибка при очистке базы данных: ${e.message}", e)
            }
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            usersReference.child(currentUser.uid).child("newsSourceCountry").setValue(country)
                .addOnSuccessListener {
                    val updatedUser =
                        userPreferences.getUserData()?.copy(newsSourceCountry = country)
                    if (updatedUser != null) {
                        userPreferences.saveUserData(updatedUser)
                        ioScope.launch {
                            _userData.value = updatedUser
                        }
                    }
                }
                .addOnFailureListener { error ->
                    ioScope.launch {
//                        _error.emit("Failed to save source country: ${error.message}")
                    }
                }
        } else {
        }
    }


    override fun fetchIsSuccessAuth(): SharedFlow<Boolean> {
        return isSuccess
    }


    override fun observeAuthState(): StateFlow<FirebaseUser?> {
        auth.addAuthStateListener {
            _user.value = auth.currentUser
        }
        return user
    }

    override fun fetchErrorSignIn(): SharedFlow<String> {
        return signInError
    }

    override fun fetchErrorSignUp(): SharedFlow<String> {
        return signUpError
    }

    override fun fetchErrorForgotPassword(): SharedFlow<String> {
        return forgotPasswordError
    }


    override fun fetchUserData(): StateFlow<UserEntity?> {
        val user = userPreferences.getUserData()
        _userData.value = user
        return userData
    }

    override fun fetchContentLanguage(): StateFlow<String> {
        return contentLanguage
    }

    override fun fetchSourceCountry(): StateFlow<String> {
        return sourceCountry
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            firebaseAuth.currentUser?.let { firebaseUser ->
                usersReference.child(firebaseUser.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userData =
                                snapshot.getValue<UserEntity>() as UserEntity
                            ioScope.launch {
                                newsDao.deleteAllNews()
                            }
                            userPreferences.saveContentLanguage(userData.newsContent)
                            userPreferences.saveSourceCountry(userData.newsSourceCountry)
                            _userData.value = userData
                            userPreferences.saveUserData(userData)
                            _contentLanguage.value = userData.newsContent
                            _sourceCountry.value = userData.newsSourceCountry

                            _isUserDataUpdatedFlow.value = true // Уведомляем, что данные обновлены


                            Log.d("CheckChangedUserData", "AfterSaveContent ${getContentLanguage()}")
                            Log.d("CheckChangedUserData", "content ${_contentLanguage.value} \n ${_sourceCountry.value}")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(
                                "UserRepositoryImpl",
                                "Error fetching user data: ${error.message}"
                            )
                        }

                    })
            }
        }
    }

}