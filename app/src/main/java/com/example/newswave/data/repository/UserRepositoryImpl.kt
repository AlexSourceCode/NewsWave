package com.example.newswave.data.repository

import androidx.lifecycle.viewModelScope
import com.example.newswave.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : UserRepository {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user.asStateFlow()

    private var _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> get() = _error.asSharedFlow()

    private var _isSuccess = MutableSharedFlow<Boolean>()
    val isSuccess: SharedFlow<Boolean> get() = _isSuccess.asSharedFlow()

    override fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                ioScope.launch {
                    _isSuccess.emit(true)
                }
            }
            .addOnFailureListener { error ->
                ioScope.launch {
                    _error.emit(error.message.toString())
                }
            }
    }


    override fun signInByEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnFailureListener { error ->
                ioScope.launch {
                    _error.emit(error.message.toString())
                }
            }
    }

    override fun signUpByEmail(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnFailureListener { error ->
                ioScope.launch {
                    _error.emit(error.message.toString())
                }
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

    override fun fetchErrorAuth(): SharedFlow<String> {
        return error
    }

}