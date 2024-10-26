package com.example.newswave.domain.entity

data class UserEntity(
    val id: String,
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
){
    constructor() : this("","","","","","")
}
