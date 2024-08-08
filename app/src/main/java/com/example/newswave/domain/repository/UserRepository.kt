package com.example.newswave.domain.repository

interface UserRepository {

    fun forgotPassword()

    fun loginByEmail()

    fun registerByEmail()
}