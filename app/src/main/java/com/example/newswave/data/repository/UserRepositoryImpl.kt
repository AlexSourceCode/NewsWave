package com.example.newswave.data.repository

import android.util.Log
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val dataBase: FirebaseDatabase
) : UserRepository {

    private val usersReference = dataBase.getReference("Users")

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user.asStateFlow()

    private var _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> get() = _error.asSharedFlow()

    private var _userData = MutableStateFlow<UserEntity?>(null)
    val userData: StateFlow<UserEntity?> get() = _userData.asStateFlow()

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
            .addOnSuccessListener {}
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
            .addOnSuccessListener { authResult ->
                val firebase = authResult.user
                if (firebase != null) {
                    val user = UserEntity(
                        id = firebase.uid,
                        username,
                        email,
                        password,
                        firstName,
                        lastName
                    )
                    usersReference.child(user.id).setValue(user)
                }
            }
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
            Log.d("UserRepositoryImpl", "observeAuthState")
            _user.value = auth.currentUser
        }
        return user
    }

    override fun fetchErrorAuth(): SharedFlow<String> {
        return error
    }

    override fun fetchUserData(): StateFlow<UserEntity?> {
        return userData
    }

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            currentUser?.let { user ->
                usersReference.child(user.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val userData = dataSnapshot.getValue(UserEntity::class.java)
                            _userData.value = userData
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.w(
                                "UserRepositoryImpl", "Failed to read value.",
                                databaseError.toException()
                            )
                        }
                    })
            }
        }
    }

}